@file:OptIn(ExperimentalAtomicApi::class)

package com.carmabs.emanate.core.source

import com.carmabs.emanate.core.Emanate
import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.action.EmaActionDispatcher
import com.carmabs.emanate.core.action.EmaEventConsumer
import com.carmabs.emanate.core.action.EventDispatcher
import com.carmabs.emanate.core.broadcast.EmaActionBroadcaster
import com.carmabs.emanate.core.error.runHandlingErrors
import com.carmabs.emanate.core.model.EmaConfiguration
import com.carmabs.emanate.core.savestate.EmaBackupValue
import com.carmabs.emanate.core.savestate.EmaRestoreState
import com.carmabs.emanate.core.sideeffect.SideEffectResult
import com.carmabs.emanate.core.source.middleware.EmaMiddleware
import com.carmabs.emanate.core.source.middleware.MiddlewareContext
import com.carmabs.emanate.core.source.middleware.MiddlewareScope
import com.carmabs.emanate.core.source.reducer.EmaReducer
import com.carmabs.emanate.core.source.reducer.ReducerScope
import com.carmabs.emanate.core.source.reducer.reducerOf
import com.carmabs.emanate.core.source.sideeffect.SideEffect
import com.carmabs.emanate.core.source.sideeffect.SideEffectConcurrency
import com.carmabs.emanate.core.source.sideeffect.SideEffectScope
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import kotlin.concurrent.Volatile
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Creates a source. Its actions are of type [A], plus the ones it listens to in the broadcasts block of setup: every
 * action goes through the middlewares, the reducers and the side effects of its type. The source creates its own scope
 * with [dispatcher], so call [EmaSource.close] when it isn't needed anymore to cancel its work.
 *
 * @param configuration applied by the source. By default the one of [Emanate], read when the source is created.
 * @param dispatcher where the source runs. By default the main dispatcher of [configuration], as the state is usually
 * drawn by the view; use cases run on their own dispatcher anyway.
 * @param setup adds the middlewares, reducers, side effects, broadcast listeners and saved values of the source. The
 * saved values are written and restored with an [com.carmabs.emanate.core.savestate.EmaStateSaver].
 */
@Suppress("FunctionName") // Factory named like the class
fun <S : EmaState, A : EmaAction, E : EmaEvent> EmaSource(
    initialState: S,
    configuration: EmaConfiguration = Emanate.configuration,
    dispatcher: CoroutineDispatcher = configuration.dispatchers.main,
    setup: SourceSetupScope<S, A, E>.() -> Unit
): EmaSource<S, A, E> {
    val sourceSetup = buildSourceSetup(setup)
    return EmaSource(
        mutableStateFlow = MutableStateFlow(initialState),
        dispatcher = dispatcher,
        middlewares = sourceSetup.middlewares,
        reducers = sourceSetup.reducers,
        sideEffects = sourceSetup.sideEffects,
        broadcastListeners = sourceSetup.broadcastListeners,
        savedValues = sourceSetup.savedValues,
        container = null,
        eventDispatcher = EventDispatcher(),
        configuration = configuration
    )
}

/**
 * Created by Carlos Mateo Benito on 11/10/2026.
 *
 * Source of a state handled with actions, in the style of Redux. Every step runs in a single coroutine, one after
 * another, and each new state is published as soon as the reducers return it. It doesn't depend on any view model,
 * so it can be used on its own.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
class EmaSource<S : EmaState, A : EmaAction, E : EmaEvent> internal constructor(
    private val mutableStateFlow: MutableStateFlow<S>,
    dispatcher: CoroutineDispatcher,
    private val middlewares: List<EmaMiddleware<S, A, *>>,
    reducers: List<EmaReducer<S, *>>,
    private val sideEffects: List<SideEffect<S, A, E, *>>,
    broadcastListeners: List<KClass<out EmaAction>>,
    internal val savedValues: List<EmaBackupValue<S, *>>,
    container: Any?,
    private val eventDispatcher: EventDispatcher<E>,
    internal val configuration: EmaConfiguration
) : EmaEventConsumer<E>,
    EmaActionDispatcher<A> {

    /**
     * Scope of all the work of the source, cancelled by [close]
     */
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    /**
     * Job of the side effects of the last action given to [close]. [close] doesn't cancel them, so they can finish, up
     * to their timeout or the closeTimeout of the configuration: it completes when they do.
     */
    private val closingJob = SupervisorJob()

    private val closingScope = CoroutineScope(closingJob + dispatcher)

    /**
     * Whether the source is processing the last action given to [close]. It is only read and written in the coroutine
     * of the source.
     */
    private var closing = false

    /**
     * Reported as the class that launches the side effects: the view model that owns the source, or the source
     */
    internal val container: Any = container ?: this

    /**
     * The saved values are restored before the reducers of setup, so they receive the restored state
     */
    private val reducers: List<EmaReducer<S, *>> =
        listOfNotNull(savedValues.takeIf { it.isNotEmpty() }?.let { restoreReducer(it) }) + reducers

    val stateFlow: StateFlow<S> = mutableStateFlow.asStateFlow()

    val state: S
        get() = mutableStateFlow.value

    /**
     * Steps pending to run, one after another: new actions, events and the middleware chains continued
     * asynchronously
     */
    private val queue = Channel<() -> Unit>(Channel.UNLIMITED)

    private val middlewareContext = MiddlewareContext(this)

    private val sideEffectScope = SideEffectScope<S, A, E>(this)

    /**
     * Sends and receives the broadcast actions: the one of the configuration
     */
    internal val broadcaster: EmaActionBroadcaster = configuration.broadcaster

    /**
     * Asynchronous work running with a key: the id of a nextAfter or a side effect that isn't launched for every
     * action
     */
    private val jobsByKey = AtomicReference<Map<Any, Job>>(emptyMap())

    @Volatile
    private var observers: List<EmaSourceObserver<S, E>> = emptyList()

    /**
     * Adds an observer of the actions, states and events of the source. Add it before the first action to observe
     * everything.
     */
    internal fun addObserver(observer: EmaSourceObserver<S, E>) {
        observers = observers + observer
    }

    /**
     * Receives the broadcast actions of the types listened to while the source is open. It starts undispatched, so the
     * source is already listening when it is created.
     */
    private val broadcastJob: Job? = broadcastListeners.takeIf { it.isNotEmpty() }?.let { actionTypes ->
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            broadcaster.listen(actionTypes).collect { dispatchAction(it) }
        }
    }

    init {
        // Runs the steps one after another
        scope.launch {
            try {
                for (step in queue) {
                    step()
                }
            } finally {
                // The queue is closed in close, after the pending steps. If a step throws an exception with the Throw
                // policy, the source stops too, so its work is cancelled.
                scope.cancel()
                closingJob.complete()
            }
        }
    }

    /**
     * Processes the pending actions and [lastAction], and then cancels the asynchronous work of the source, except the
     * side effects of [lastAction], which can finish, up to their timeout or the closeTimeout of the configuration.
     * Actions dispatched after it are ignored.
     *
     * @param lastAction last action of the source, whose side effects can finish after it is closed, for example to
     * send a result to the previous screen. Null to close it without a last action.
     * @return the job that completes when the source has finished, including the side effects of [lastAction]
     */
    fun close(lastAction: EmaAction? = null): Job {
        broadcastJob?.cancel()
        lastAction?.let { action ->
            queue.trySend {
                closing = true
                observers.forEach { it.onAction(action) }
                runChain(0, action)
            }
        }
        queue.close()
        // It is completed when the steps finish, and then waits for the side effects of the last action
        return closingJob
    }

    final override fun dispatch(action: A) {
        dispatchAction(action)
    }

    /**
     * Runs [step] in the coroutine of the source, after the actions dispatched before it
     */
    internal fun post(step: () -> Unit) {
        queue.trySend(step)
    }

    /**
     * Restores the values of [EmaRestoreState] with the saved values of setup, in the order they were added. A value
     * that can't be decoded, or whose restore fails with the Catch policy, is skipped.
     */
    private fun restoreReducer(savedValues: List<EmaBackupValue<S, *>>) =
        reducerOf<S, EmaRestoreState>(EmaRestoreState::class) { action ->
            savedValues.fold(state) { restoredState, savedValue ->
                val value = action.values[savedValue.key] ?: return@fold restoredState
                var newState = restoredState
                configuration.runHandlingErrors(container, { "restore ${savedValue.key}" }) {
                    newState = savedValue.restore(restoredState, value)
                }
                newState
            }
        }

    private fun <T> EmaBackupValue<S, T>.restore(state: S, value: String): S =
        codec.decode(value)?.let { restore(state, it) } ?: state

    /**
     * Dispatches an action without checking its type. The scopes only let dispatch actions of type [A].
     */
    internal fun dispatchAction(action: EmaAction) {
        queue.trySend {
            observers.forEach { it.onAction(action) }
            runChain(0, action)
        }
    }

    private fun reduce(action: EmaAction) {
        val newState = reducers.fold(state) { previousState, reducer ->
            reduce(reducer, previousState, action)
        }
        mutableStateFlow.value = newState
        observers.forEach { it.onState(newState) }
    }

    /**
     * Reduces [action] with [reducer] if it is of its type
     */
    private fun <F : EmaAction> reduce(reducer: EmaReducer<S, F>, state: S, action: EmaAction): S {
        if (!reducer.actionType.isInstance(action)) return state
        return with(reducer) { ReducerScope(state).reduce(reducer.actionType.cast(action)) }
    }

    /**
     * Runs the middleware at [index] with [action]. When all the middlewares have called next, the reducers run.
     * A middleware of other type of actions is skipped.
     */
    private fun runMiddleware(index: Int, action: EmaAction) {
        if (index == middlewares.size) {
            reduce(action)
            launchSideEffects(action)
            return
        }
        val middleware = middlewares[index]
        if (!middleware.actionType.isInstance(action)) {
            runMiddleware(index + 1, action)
            return
        }
        val running = AtomicBoolean(true)
        val middlewareScope = MiddlewareScope<S, A>(middlewareContext, action, running) { nextAction ->
            if (running.load()) {
                runMiddleware(index + 1, nextAction)
            } else {
                // Called after the middleware has finished, for example from nextAfter: the chain continues in the
                // source coroutine so the reducers never run at the same time
                queue.trySend { runChain(index + 1, nextAction) }
            }
        }
        try {
            invoke(middleware, middlewareScope, action)
        } finally {
            running.store(false)
        }
    }

    /**
     * Runs the chain of [action] from the middleware at [index]. If a middleware or a reducer throws an exception, the
     * error policy decides: with Catch the action is discarded without changing the state, and the source keeps working.
     */
    private fun runChain(index: Int, action: EmaAction) {
        // With Throw the exception stops the source. With Catch it is reported with the action that failed, and its
        // stack trace shows the middleware or the reducer.
        configuration.runHandlingErrors(container, { "action ${action::class.simpleName}" }) {
            runMiddleware(index, action)
        }
    }

    private fun <F : EmaAction> invoke(
        middleware: EmaMiddleware<S, A, F>,
        middlewareScope: MiddlewareScope<S, A>,
        action: EmaAction
    ) {
        with(middleware) { middlewareScope.invoke(middleware.actionType.cast(action)) }
    }

    private fun launchSideEffects(action: EmaAction) {
        // The type is checked before launching, so other actions don't cancel nor block a side effect
        sideEffects.filter { it.actionType.isInstance(action) }.forEach { launchSideEffect(it, action) }
    }

    private fun <F : EmaAction> launchSideEffect(sideEffect: SideEffect<S, A, E, F>, action: EmaAction) {
        val typedAction = sideEffect.actionType.cast(action)
        // The side effect itself is the key of its running job
        val key = when (sideEffect.concurrency) {
            SideEffectConcurrency.Every -> null

            SideEffectConcurrency.Latest -> sideEffect

            SideEffectConcurrency.Leading -> {
                // Side effects are launched only from the source coroutine, so no other one can start meanwhile
                if (jobsByKey.load()[sideEffect]?.isActive == true) return
                sideEffect
            }
        }
        // The source is closed right after its last action, so its side effects run in a scope that close doesn't
        // cancel, with a timeout so they don't keep the source alive
        with(sideEffect) {
            val closeTimeout = configuration.closeTimeout
            launch(
                key = key,
                timeout = if (closing) timeout ?: closeTimeout else timeout,
                onTimeout = { sideEffectScope.onTimeout(typedAction) },
                launchScope = if (closing) closingScope else scope,
                block = { sideEffectScope.invoke(typedAction) }
            )
        }
    }

    /**
     * Launches asynchronous work applying the side effect configuration of Emanate.
     * @param key if work with the same key is running, it is cancelled
     * @param timeout if the work doesn't finish in this time, it is cancelled and [onTimeout] is called
     * @param launchScope where it is launched. By default the scope of the source, cancelled by [close].
     */
    internal fun launch(
        key: Any?,
        timeout: Duration?,
        onTimeout: () -> Unit,
        launchScope: CoroutineScope = scope,
        block: suspend () -> Unit
    ): Job {
        key?.let { removeJob(it)?.cancel() }
        val job = SideEffectResult(container = container, scope = launchScope, configuration = configuration) {
            if (timeout == null) {
                block()
            } else if (withTimeoutOrNull(timeout) { block() } == null) {
                onTimeout()
            }
        }.job
        key?.let {
            putJob(it, job)
            job.invokeOnCompletion { _ -> removeJob(it, job) }
        }
        return job
    }

    /**
     * The event goes through the queue, so it arrives after the actions dispatched before it and the view sees
     * their state when it receives the event
     */
    internal fun postEvent(event: E, allowDuplicated: Boolean) {
        queue.trySend {
            eventDispatcher.postEvent(event, allowDuplicated)
            observers.forEach { it.onEvent(event) }
        }
    }

    final override fun consumeEvent(event: E) = eventDispatcher.consumeEvent(event)

    final override val eventFlow: Flow<List<E>> = eventDispatcher.eventFlow

    private fun putJob(key: Any, job: Job) {
        updateJobs { it + (key to job) }
    }

    /**
     * Removes the job of [key] and returns it
     */
    private fun removeJob(key: Any): Job? {
        var removed: Job? = null
        updateJobs { jobs ->
            removed = jobs[key]
            jobs - key
        }
        return removed
    }

    /**
     * Removes the job of [key] only if it is [job], so a newer job with the same key is kept
     */
    private fun removeJob(key: Any, job: Job) {
        updateJobs { jobs -> if (jobs[key] === job) jobs - key else jobs }
    }

    private inline fun updateJobs(transform: (Map<Any, Job>) -> Map<Any, Job>) {
        while (true) {
            val jobs = jobsByKey.load()
            if (jobsByKey.compareAndSet(jobs, transform(jobs))) return
        }
    }
}
