@file:OptIn(ExperimentalAtomicApi::class)

package com.carmabs.emanate.core.source

import com.carmabs.emanate.core.Ema
import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.action.EmaActionDispatcher
import com.carmabs.emanate.core.model.emaSideEffect
import com.carmabs.emanate.core.source.middleware.EmaMiddleware
import com.carmabs.emanate.core.source.middleware.MiddlewareContext
import com.carmabs.emanate.core.source.middleware.MiddlewareScope
import com.carmabs.emanate.core.source.reducer.EmaReducer
import com.carmabs.emanate.core.source.reducer.ReducerScope
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import kotlin.concurrent.Volatile
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.reflect.cast
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Creates a source. Its actions are of type [A]: every action goes through the middlewares and the reducers of its
 * type. The source
 * creates its own scope with [dispatcher], so call [EmaSource.close] when it isn't needed anymore to cancel its
 * work.
 *
 * @param dispatcher where the source runs. By default the main dispatcher of Ema, as the state is usually drawn by
 * the view; use cases run on their own dispatcher anyway.
 * @param setup adds the middlewares and the reducers of the source
 */
@Suppress("FunctionName") // Factory named like the class
fun <S : EmaState, A : EmaAction, E : EmaEvent> EmaSource(
    initialState: S,
    dispatcher: CoroutineDispatcher = Ema.configuration.dispatchers.main,
    setup: SourceSetupScope<S, A, E>.() -> Unit
): EmaSource<S, A, E> {
    val setupScope = SourceSetupScope<S, A, E>().apply {
        setup()
        build()
    }
    return EmaSource(
        mutableStateFlow = MutableStateFlow(initialState),
        dispatcher = dispatcher,
        middlewares = setupScope.middlewareList,
        reducers = setupScope.reducerList
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
    private val reducers: List<EmaReducer<S, *>>
) : EmaActionDispatcher<A> {

    /**
     * Scope of all the work of the source, cancelled by [close]
     */
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    val stateFlow: StateFlow<S> = mutableStateFlow.asStateFlow()

    val state: S
        get() = mutableStateFlow.value

    /**
     * Steps pending to run, one after another: new actions and the middleware chains continued asynchronously
     */
    private val queue = Channel<() -> Unit>(Channel.UNLIMITED)

    private val middlewareContext = MiddlewareContext(this)

    /**
     * Asynchronous work running with a key, for example the id of a nextAfter
     */
    private val jobsByKey = AtomicReference<Map<Any, Job>>(emptyMap())

    @Volatile
    private var observers: List<EmaSourceObserver<S, E>> = emptyList()

    /**
     * Adds an observer of the actions and states of the source. Add it before the first action to observe
     * everything.
     */
    internal fun addObserver(observer: EmaSourceObserver<S, E>) {
        observers = observers + observer
    }

    private val loop: Job = scope.launch {
        for (step in queue) {
            step()
        }
        // The queue is closed in close, after the pending steps
        scope.cancel()
    }

    /**
     * Processes the pending actions and then cancels the asynchronous work of the source. Actions dispatched after
     * it are ignored.
     * @return the job that completes when the source has finished
     */
    fun close(): Job {
        queue.close()
        return loop
    }

    final override fun dispatch(action: A) {
        dispatchAction(action)
    }

    /**
     * Dispatches an action without checking its type. The scopes only let dispatch actions of type [A].
     */
    internal fun dispatchAction(action: EmaAction) {
        queue.trySend {
            observers.forEach { it.onAction(action) }
            runMiddleware(0, action)
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
                queue.trySend { runMiddleware(index + 1, nextAction) }
            }
        }
        try {
            invoke(middleware, middlewareScope, action)
        } finally {
            running.store(false)
        }
    }

    private fun <F : EmaAction> invoke(
        middleware: EmaMiddleware<S, A, F>,
        middlewareScope: MiddlewareScope<S, A>,
        action: EmaAction
    ) {
        with(middleware) { middlewareScope.invoke(middleware.actionType.cast(action)) }
    }

    /**
     * Launches asynchronous work applying the side effect configuration of Ema.
     * @param key if work with the same key is running, it is cancelled
     * @param timeout if the work doesn't finish in this time, it is cancelled and [onTimeout] is called
     */
    internal fun launch(key: Any?, timeout: Duration?, onTimeout: () -> Unit, block: suspend () -> Unit): Job {
        key?.let { removeJob(it)?.cancel() }
        val job = emaSideEffect(container = this, scope = scope) {
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
