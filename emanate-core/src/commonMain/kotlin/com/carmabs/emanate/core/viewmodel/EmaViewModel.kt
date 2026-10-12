package com.carmabs.emanate.core.viewmodel

import com.carmabs.emanate.core.Emanate
import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.action.EmaAction.Initializer
import com.carmabs.emanate.core.action.EmaAction.Lifecycle
import com.carmabs.emanate.core.action.EmaActionDispatcher
import com.carmabs.emanate.core.action.EventDispatcher
import com.carmabs.emanate.core.logging.EmaLogger
import com.carmabs.emanate.core.source.EmaSource
import com.carmabs.emanate.core.source.EmaSourceObserver
import com.carmabs.emanate.core.source.SourceSetup
import com.carmabs.emanate.core.source.SourceSetupScope
import com.carmabs.emanate.core.source.buildSourceSetup
import com.carmabs.emanate.core.source.middleware.loggerMiddleware
import com.carmabs.emanate.core.source.middleware.middlewareOf
import com.carmabs.emanate.core.source.middleware.receivedActionLoggerMiddleware
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
 * @param dispatcher where the view model runs. By default the main dispatcher of Emanate.
 * @param logger logs every action with the state before and after it, and in one line when it is received, so the
 * actions stopped by a middleware are also logged. By default the logger of the configuration of Emanate. Null to
 * disable the logs of this view model.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
abstract class EmaViewModel<S : EmaState, A : EmaAction, E : EmaEvent>(
    initialDataState: S,
    private val dispatcher: CoroutineDispatcher = Emanate.configuration.dispatchers.main,
    private val logger: EmaLogger? = Emanate.configuration.logger
) : EmaViewModelContract<S, E>,
    EmaActionDispatcher<A> {

    final override val initialState: S = initialDataState

    /**
     * Returns the middlewares, reducers, side effects and broadcast listeners of the view model, created with [source]:
     * `override fun setup() = source { ... }`. Besides the actions of type [A], they can handle the [Initializer] of
     * the screen, its [Lifecycle] actions and the actions broadcast by other view models that it listens to. It is
     * called once, when the source is first needed.
     */
    protected abstract fun setup(): SourceSetup<S, A, E>

    /**
     * Creates the setup of the view model with its types, so they don't have to be written in [setup].
     */
    protected fun source(setup: SourceSetupScope<S, A, E>.() -> Unit): SourceSetup<S, A, E> = buildSourceSetup(setup)

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
        emaSource.addObserver(observer)
    }

    /**
     * The source is created when it is first needed, not in the constructor: [setup] can use the properties of the
     * subclass, which are assigned after the constructor of this class
     */
    private val emaSource: EmaSource<S, EmaAction, E> by lazy { createSource() }

    /**
     * The source receives the actions of the screen, the initializer and the lifecycle actions
     */
    private fun createSource(): EmaSource<S, EmaAction, E> {
        val sourceSetup = setup()
        val tag = this::class.simpleName.orEmpty()
        return EmaSource(
            mutableStateFlow = mutableStateFlow,
            dispatcher = dispatcher,
            // The full log goes last, right before the reducers, so it logs the state they return
            middlewares = listOfNotNull(
                logger?.let { receivedActionLoggerMiddleware(tag, it) },
                updateTrackerMiddleware
            ) + sourceSetup.middlewares + listOfNotNull(logger?.let { loggerMiddleware(tag, it) }),
            reducers = sourceSetup.reducers,
            sideEffects = sourceSetup.sideEffects,
            broadcastListeners = sourceSetup.broadcastListeners,
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
        emaSource.dispatch(action)
    }

    /**
     * Called when the view is created. The first time, the source receives [initializer], or [Initializer.EMPTY] if
     * there isn't any.
     */
    final override fun onCreated(initializer: Initializer?) {
        if (!hasBeenInitialized) {
            hasBeenInitialized = true
            emaSource.dispatch(initializer ?: Initializer.EMPTY)
        }
    }

    final override fun onStartView() {
        emaSource.dispatch(Lifecycle.Started)
    }

    /**
     * Called when view is shown in foreground
     */
    final override fun onResumeView() {
        emaSource.dispatch(Lifecycle.Resumed)
    }

    /**
     * Called when view is hidden in background
     */
    final override fun onPauseView() {
        emaSource.dispatch(Lifecycle.Paused)
    }

    final override fun onStopView() {
        emaSource.dispatch(Lifecycle.Stopped)
    }

    final override fun consumeEvent(event: E) {
        eventDispatcher.consumeEvent(event)
    }

    /**
     * Called when the view model is destroyed. The source processes [Lifecycle.Destroyed] after the pending actions
     * and then cancels all its pending work.
     */
    final override fun onCleared() {
        emaSource.dispatch(Lifecycle.Destroyed)
        emaSource.close()
    }
}
