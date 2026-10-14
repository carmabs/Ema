package com.carmabs.emanate.core.viewmodel

import com.carmabs.emanate.core.Emanate
import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.action.EmaActionDispatcher
import com.carmabs.emanate.core.action.EventDispatcher
import com.carmabs.emanate.core.initializer.EmaInitializer
import com.carmabs.emanate.core.model.EmaConfiguration
import com.carmabs.emanate.core.savestate.EmaBackup
import com.carmabs.emanate.core.savestate.EmaStateSaver
import com.carmabs.emanate.core.savestate.EmaStateStore
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Created by Carlos Mateo Benito on 11/10/2026.
 *
 * View model whose state is handled by an [EmaSource]: the actions of the screen, the initializer and the lifecycle
 * of the view go through the middlewares, the reducers and the side effects added in [setup]. It is the recommended
 * view model. It can save parts of its state with an EmaBackup installed in [setup], to restore them after the
 * system kills the process of the app.
 *
 * @param initialDataState state of the view before any action
 * @param configuration applied by the view model and its source. By default the one of [Emanate], read when the view
 * model is created; pass one to configure this view model without depending on [Emanate.init], for example in a
 * library or in a test. The view model runs on its main dispatcher, and its logger logs every action with the state
 * before and after it, and in one line when it is received, so the actions stopped by a middleware are also logged.
 * Pass a configuration without logger to disable the logs of this view model.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
abstract class EmaViewModel<S : EmaState, A : EmaAction, E : EmaEvent>(
    initialDataState: S,
    private val configuration: EmaConfiguration = Emanate.configuration
) : EmaViewModelContract<S, E>,
    EmaActionDispatcher<A> {

    final override val initialState: S = initialDataState

    /**
     * Returns the middlewares, reducers, side effects, broadcast listeners and plugins of the view model, created
     * with [source]: `override fun setup() = source { ... }`. Besides the actions of type [A], they can handle the
     * [EmaInitializer] of the screen, its [EmaLifecycle] actions and the actions broadcast by other view models that it
     * listens to. It is called once, when the source or its backups are first needed.
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
        if (action !is EmaLifecycle && action != EmaInitializer.EMPTY && action != EmaAction.EMPTY) {
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
     * Saves the values of the backups installed in [setup] and restores them. Null without a store or backups.
     */
    @Volatile
    private var stateSaver: EmaStateSaver<S>? = null

    /**
     * Saves the values of the backups installed in [setup] in [store], and restores the ones it has when the view is
     * created. Call it once, before the view is created. Each platform calls it with its own store: on Android,
     * EmaAndroidViewModel with the SavedStateHandle of the screen. It is internal: emanate-android and emanate-test use
     * it as friend modules.
     *
     * @throws IllegalStateException if the view model has backups and the view has already been created or it already
     * has a store
     */
    internal fun attachStateStore(store: EmaStateStore) {
        // The backups are plugins of the setup of type S, so they back up the state of this view model
        @Suppress("UNCHECKED_CAST")
        val backups = sourceSetup.plugins.filterIsInstance<EmaBackup<*>>() as List<EmaBackup<S>>
        if (backups.isEmpty()) return
        check(!hasBeenInitialized) {
            "The state store of ${this::class.simpleName} must be attached before the view is created"
        }
        check(stateSaver == null) { "${this::class.simpleName} already has a state store" }
        stateSaver = EmaStateSaver(emaSource, store, *backups.toTypedArray())
    }

    /**
     * What [setup] returns, created once. It is internal: emanate-test checks it as a friend module.
     */
    internal val sourceSetup: SourceSetup<S, A, E> by lazy { setup() }

    /**
     * The source receives the actions of the screen, the initializer and the lifecycle actions
     */
    private fun createSource(): EmaSource<S, EmaAction, E> {
        val tag = this::class.simpleName.orEmpty()
        return EmaSource(
            mutableStateFlow = mutableStateFlow,
            dispatcher = configuration.dispatchers.main,
            // The full log goes last, right before the reducers, so it logs the state they return
            middlewares = listOfNotNull(
                configuration.logger?.let { receivedActionLoggerMiddleware(tag, it) },
                updateTrackerMiddleware
            ) + sourceSetup.middlewares + listOfNotNull(
                configuration.logger?.let { loggerMiddleware(tag, it, configuration.printer) }
            ),
            reducers = sourceSetup.reducers,
            sideEffects = sourceSetup.sideEffects,
            closeEffects = sourceSetup.closeEffects,
            broadcastListeners = sourceSetup.broadcastListeners,
            container = this,
            eventDispatcher = eventDispatcher,
            configuration = configuration
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
     * Called when the view is created. The first time, the source receives [initializer], or [EmaInitializer.EMPTY] if
     * there isn't any, and then the saved values, if it has a store.
     */
    final override fun onCreated(initializer: EmaInitializer?) {
        if (!hasBeenInitialized) {
            hasBeenInitialized = true
            emaSource.dispatch(initializer ?: EmaInitializer.EMPTY)
            stateSaver?.restore()
        }
    }

    final override fun onStartView() {
        emaSource.dispatch(EmaLifecycle.Started)
    }

    /**
     * Called when view is shown in foreground
     */
    final override fun onResumeView() {
        emaSource.dispatch(EmaLifecycle.Resumed)
    }

    /**
     * Called when view is hidden in background
     */
    final override fun onPauseView() {
        emaSource.dispatch(EmaLifecycle.Paused)
    }

    final override fun onStopView() {
        emaSource.dispatch(EmaLifecycle.Stopped)
    }

    final override fun consumeEvent(event: E) {
        eventDispatcher.consumeEvent(event)
    }

    /**
     * Called when the view model is destroyed. The source processes [EmaLifecycle.Destroyed] after the pending actions
     * and then it is closed: its work is cancelled, except its onClose blocks and the side effects that survive close,
     * which can finish.
     */
    final override fun onCleared() {
        emaSource.dispatch(EmaLifecycle.Destroyed)
        emaSource.close()
    }
}
