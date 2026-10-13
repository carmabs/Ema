package com.carmabs.emanate.core.source

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.savestate.BackupScope
import com.carmabs.emanate.core.source.middleware.MiddlewareListScope
import com.carmabs.emanate.core.source.reducer.ReducerListScope
import com.carmabs.emanate.core.source.sideeffect.SideEffectListScope
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState

/**
 * Created by Carlos Mateo Benito on 11/10/2026.
 *
 * Adds the middlewares, reducers, side effects and broadcast listeners of the source, each one in its own block, and
 * the parts of the state saved by the view model. The blocks can be used more than once: what they add is kept in
 * order. It can only be used inside setup.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
@EmaSourceDsl
class SourceSetupScope<S : EmaState, A : EmaAction, E : EmaEvent> internal constructor() {

    private val guard = SetupGuard()

    private val middlewareListScope = MiddlewareListScope<S, A>(guard)

    private val reducerListScope = ReducerListScope<S>(guard)

    private val sideEffectListScope = SideEffectListScope<S, A, E>(guard)

    private val broadcastListScope = BroadcastListScope(guard)

    private val savedStateListScope = BackupScope<S>(guard)

    /**
     * Adds middlewares. They handle the actions in the order they are added, before the reducers.
     */
    fun middlewares(block: MiddlewareListScope<S, A>.() -> Unit) {
        guard.checkNotBuilt()
        middlewareListScope.block()
    }

    /**
     * Adds reducers. Each one only receives the actions of its type, in the order they are added.
     */
    fun reducers(block: ReducerListScope<S>.() -> Unit) {
        guard.checkNotBuilt()
        reducerListScope.block()
    }

    /**
     * Adds side effects. Each one runs asynchronously after the reducers of the actions of its type.
     */
    fun sideEffects(block: SideEffectListScope<S, A, E>.() -> Unit) {
        guard.checkNotBuilt()
        sideEffectListScope.block()
    }

    /**
     * Adds the types of the broadcast actions the source listens to.
     */
    fun broadcasts(block: BroadcastListScope.() -> Unit) {
        guard.checkNotBuilt()
        broadcastListScope.block()
    }

    /**
     * Adds the parts of the state to save, so they are restored after the system kills the process of the app. Save
     * only what the user would lose, like the fields of a form, not the whole state: the platform limits the size of
     * the saved values. Each value is saved every time it changes and, when the view model is created again, they are
     * restored after its initializer, before the reducers.
     *
     * An EmaViewModel saves them in the store of its platform: on Android, the SavedStateHandle of the screen. A source
     * on its own saves them with an [com.carmabs.emanate.core.savestate.EmaStateSaver]. Without a store nothing is
     * saved.
     */
    fun backup(block: BackupScope<S>.() -> Unit) {
        guard.checkNotBuilt()
        savedStateListScope.block()
    }

    /**
     * Called when setup finishes. Nothing can be added after it.
     */
    internal fun build(): SourceSetup<S, A, E> {
        guard.build()
        return SourceSetup(
            middlewares = middlewareListScope.middlewareList,
            reducers = reducerListScope.reducerList,
            sideEffects = sideEffectListScope.sideEffectList,
            broadcastListeners = broadcastListScope.broadcastListenerList,
            savedValues = savedStateListScope.savedValueList
        )
    }
}
