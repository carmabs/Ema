package com.carmabs.emanate.core.viewmodel

import com.carmabs.emanate.core.Ema
import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.action.EmaAction.Initializer
import com.carmabs.emanate.core.action.EmaAction.Lifecycle
import com.carmabs.emanate.core.action.EmaActionDispatcher
import com.carmabs.emanate.core.action.EventDispatcher
import com.carmabs.emanate.core.logging.EmaLogger
import com.carmabs.emanate.core.source.EmaSource
import com.carmabs.emanate.core.source.EmaSourceObserver
import com.carmabs.emanate.core.source.SourceSetupScope
import com.carmabs.emanate.core.source.middleware.loggerMiddleware
import com.carmabs.emanate.core.source.middleware.middlewareOf
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import kotlin.concurrent.Volatile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Created by Carlos Mateo Benito on 11/10/2026.
 *
 * View model whose state is handled by an [EmaSource]: the actions of the screen, the initializer and the lifecycle
 * of the view go through the middlewares, the reducers and the side effects added in [setup]. It is the recommended
 * view model.
 *
 * @param initialDataState state of the view before any action
 * @param dispatcher where the view model runs. By default the main dispatcher of Ema.
 * @param logger logs every action with the state before and after it. Null to disable the logs.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
abstract class EmaViewModel<S : EmaState, A : EmaAction, E : EmaEvent>(
    initialDataState: S,
    private val dispatcher: CoroutineDispatcher = Ema.configuration.dispatchers.main,
    private val logger: EmaLogger? = EmaLogger.Println
) : EmaViewModelContract<S, E>,
    EmaActionDispatcher<A> {

    final override val initialState: S = initialDataState

    /**
     * Adds the middlewares, reducers, side effects and broadcast listeners of the view model. Besides the actions of
     * type [A], they can handle the [Initializer] of the screen, its [Lifecycle] actions and the actions broadcast by
     * other view models that it listens to.
     */
    abstract fun SourceSetupScope<S, A, E>.setup()

    /**
     * Marks the state as updated when an action of the screen or an initializer is received
     */
    private val updateTrackerMiddleware = middlewareOf<EmaState, Nothing, EmaAction>(EmaAction::class) { action ->
        if (action !is Lifecycle && action != Initializer.EMPTY && action != EmaAction.EMPTY) {
            hasBeenUpdated = true
        }
        next()
    }

    private val mutableStateFlow = MutableStateFlow(initialDataState)

    final override val stateFlow: StateFlow<S> = mutableStateFlow.asStateFlow()

    private val eventDispatcher = EventDispatcher<E>()

    final override val eventFlow: Flow<List<E>> = eventDispatcher.eventFlow

    /**
     * Adds an observer of the actions, states and events of the view model. Add it before the first action to
     * observe everything. It is internal: emanate-test uses it as a friend module.
     */
    internal fun addObserver(observer: EmaSourceObserver<S, E>) {
        source.addObserver(observer)
    }

    /**
     * The source is created when it is first needed, not in the constructor: [setup] can use the properties of the
     * subclass, which are assigned after the constructor of this class
     */
    private val source: EmaSource<S, EmaAction, E> by lazy { createSource() }

    /**
     * The source receives the actions of the screen, the initializer and the lifecycle actions
     */
    private fun createSource(): EmaSource<S, EmaAction, E> {
        val setupScope = SourceSetupScope<S, A, E>().apply {
            setup()
            build()
        }
        return EmaSource(
            mutableStateFlow = mutableStateFlow,
            dispatcher = dispatcher,
            middlewares = listOfNotNull(
                logger?.let { loggerMiddleware(this::class.simpleName.orEmpty(), it) },
                updateTrackerMiddleware
            ) + setupScope.middlewareList,
            reducers = setupScope.reducerList,
            sideEffects = setupScope.sideEffectList,
            broadcastListeners = setupScope.broadcastListenerList,
            container = this,
            eventDispatcher = eventDispatcher
        )
    }

    /**
     * To determine if the view must be updated when view model is created automatically
     */
    protected open val updateOnInitialization: Boolean = true

    /**
     * Used to know if state has been updated at least once
     */
    @Volatile
    private var hasBeenUpdated = false

    final override val shouldRenderState: Boolean
        get() = updateOnInitialization || hasBeenUpdated

    /**
     * Determine if the view model has initialized its state
     */
    private var hasBeenInitialized: Boolean = false

    final override fun dispatch(action: A) {
        source.dispatch(action)
    }

    /**
     * Called when the view is created. The first time, the source receives [initializer], or [Initializer.EMPTY] if
     * there isn't any.
     */
    final override fun onCreated(initializer: Initializer?) {
        if (!hasBeenInitialized) {
            hasBeenInitialized = true
            source.dispatch(initializer ?: Initializer.EMPTY)
        }
    }

    final override fun onStartView() {
        source.dispatch(Lifecycle.Started)
    }

    /**
     * Called when view is shown in foreground
     */
    final override fun onResumeView() {
        source.dispatch(Lifecycle.Resumed)
    }

    /**
     * Called when view is hidden in background
     */
    final override fun onPauseView() {
        source.dispatch(Lifecycle.Paused)
    }

    final override fun onStopView() {
        source.dispatch(Lifecycle.Stopped)
    }

    final override fun consumeEvent(event: E) {
        eventDispatcher.consumeEvent(event)
    }

    /**
     * Called when the view model is destroyed. The source processes [Lifecycle.Destroyed] after the pending actions
     * and then cancels all its pending work.
     */
    final override fun onCleared() {
        source.dispatch(Lifecycle.Destroyed)
        source.close()
    }
}
