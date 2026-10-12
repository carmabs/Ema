package com.carmabs.emanate.core.source

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.source.middleware.MiddlewareListScope
import com.carmabs.emanate.core.source.reducer.ReducerListScope
import com.carmabs.emanate.core.source.sideeffect.SideEffectListScope
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState

/**
 * Created by Carlos Mateo Benito on 11/10/2026.
 *
 * Adds the middlewares, reducers, side effects and broadcast listeners of the source, each one in its own block. The
 * blocks can be used more than once: what they add is kept in order. It can only be used inside setup.
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
     * Called when setup finishes. Nothing can be added after it.
     */
    internal fun build(): SourceSetup<S, A, E> {
        guard.build()
        return SourceSetup(
            middlewares = middlewareListScope.middlewareList,
            reducers = reducerListScope.reducerList,
            sideEffects = sideEffectListScope.sideEffectList,
            broadcastListeners = broadcastListScope.broadcastListenerList
        )
    }
}
