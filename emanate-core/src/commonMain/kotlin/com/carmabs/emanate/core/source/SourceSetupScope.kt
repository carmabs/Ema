package com.carmabs.emanate.core.source

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.source.middleware.MiddlewareSetupScope
import com.carmabs.emanate.core.source.reducer.ReducerSetupScope
import com.carmabs.emanate.core.source.sideeffect.SideEffectSetupScope
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState

/**
 * Created by Carlos Mateo Benito on 11/10/2026.
 *
 * Adds the middlewares, reducers, side effects and broadcast listeners of the source, each one in its own block, and
 * installs its plugins. The blocks can be used more than once: what they add is kept in order. It can only be used
 * inside setup.
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

    private val middlewareSetupScope = MiddlewareSetupScope<S, A>(guard)

    private val reducerSetupScope = ReducerSetupScope<S>(guard)

    private val sideEffectSetupScope = SideEffectSetupScope<S, A, E>(guard)

    private val broadcastSetupScope = BroadcastSetupScope(guard)

    private var pluginList: List<EmaSourcePlugin<S>> = emptyList()

    /**
     * Adds middlewares. They handle the actions in the order they are added, before the reducers.
     */
    fun middlewares(block: MiddlewareSetupScope<S, A>.() -> Unit) {
        guard.checkNotBuilt()
        middlewareSetupScope.block()
    }

    /**
     * Adds reducers. Each one only receives the actions of its type, in the order they are added.
     */
    fun reducers(block: ReducerSetupScope<S>.() -> Unit) {
        guard.checkNotBuilt()
        reducerSetupScope.block()
    }

    /**
     * Adds side effects. Each one runs asynchronously after the reducers of the actions of its type.
     */
    fun sideEffects(block: SideEffectSetupScope<S, A, E>.() -> Unit) {
        guard.checkNotBuilt()
        sideEffectSetupScope.block()
    }

    /**
     * Adds the types of the broadcast actions the source listens to.
     */
    fun broadcasts(block: BroadcastSetupScope.() -> Unit) {
        guard.checkNotBuilt()
        broadcastSetupScope.block()
    }

    /**
     * Installs [plugin], a feature that the source doesn't know, like an EmaBackup. It adds its pieces to this setup,
     * so they are kept in order with the ones added before and after it.
     */
    fun install(plugin: EmaSourcePlugin<S>) {
        guard.checkNotBuilt()
        pluginList = pluginList + plugin
        plugin.install(this)
    }

    /**
     * Called when setup finishes. Nothing can be added after it.
     */
    internal fun build(): SourceSetup<S, A, E> {
        guard.build()
        return SourceSetup(
            middlewares = middlewareSetupScope.middlewareList,
            reducers = reducerSetupScope.reducerList,
            sideEffects = sideEffectSetupScope.sideEffectList,
            closeEffects = sideEffectSetupScope.closeEffectList,
            broadcastListeners = broadcastSetupScope.broadcastListenerList,
            plugins = pluginList
        )
    }
}
