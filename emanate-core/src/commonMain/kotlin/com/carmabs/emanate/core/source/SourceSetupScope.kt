package com.carmabs.emanate.core.source

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.source.middleware.EmaMiddleware
import com.carmabs.emanate.core.source.middleware.MiddlewareScope
import com.carmabs.emanate.core.source.middleware.middlewareOf
import com.carmabs.emanate.core.source.reducer.EmaReducer
import com.carmabs.emanate.core.source.reducer.ReducerScope
import com.carmabs.emanate.core.source.reducer.reducerOf
import com.carmabs.emanate.core.source.sideeffect.SideEffect
import com.carmabs.emanate.core.source.sideeffect.SideEffectConcurrency
import com.carmabs.emanate.core.source.sideeffect.SideEffectScope
import com.carmabs.emanate.core.source.sideeffect.sideEffectOf
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import kotlin.reflect.KClass
import kotlin.time.Duration

/**
 * Created by Carlos Mateo Benito on 11/10/2026.
 *
 * Adds the middlewares, reducers, side effects and broadcast listeners of the source. It can only be used inside setup.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
@EmaSourceDsl
class SourceSetupScope<S : EmaState, A : EmaAction, E : EmaEvent> internal constructor() {

    internal var middlewareList: List<EmaMiddleware<S, A, *>> = emptyList()
        private set
    internal var reducerList: List<EmaReducer<S, *>> = emptyList()
        private set
    internal var sideEffectList: List<SideEffect<S, A, E, *>> = emptyList()
        private set
    internal var broadcastListenerList: List<KClass<out EmaAction>> = emptyList()
        private set

    private var built = false

    /**
     * Adds middlewares. They handle the actions in the order they are added, before the reducers.
     */
    fun addMiddleware(vararg middleware: EmaMiddleware<S, A, *>) {
        checkNotBuilt()
        middlewareList = middlewareList + middleware
    }

    /**
     * Implementation of the inline addMiddleware with the type of the actions.
     */
    @PublishedApi
    internal fun <F : EmaAction> addMiddleware(filter: KClass<F>, middlewareAction: MiddlewareScope<S, A>.(F) -> Unit) {
        addMiddleware(middlewareOf(filter, middlewareAction))
    }

    /**
     * Adds a middleware for the actions of type [F]. The other actions continue to the next middleware. Use EmaAction
     * to handle every action.
     */
    inline fun <reified F : EmaAction> addMiddleware(noinline middlewareAction: MiddlewareScope<S, A>.(F) -> Unit) {
        addMiddleware(F::class, middlewareAction)
    }

    /**
     * Adds reducers. Each one only receives the actions of its type.
     */
    fun addReducer(vararg reducer: EmaReducer<S, *>) {
        checkNotBuilt()
        reducerList = reducerList + reducer
    }

    /**
     * Implementation of the inline addReducer with the type of the actions.
     */
    @PublishedApi
    internal fun <F : EmaAction> addReducer(filter: KClass<F>, reduceAction: ReducerScope<S>.(F) -> S) {
        addReducer(reducerOf(filter, reduceAction))
    }

    /**
     * Adds a reducer for the actions of type [F]: of the source, initializers or lifecycle actions. With the sealed
     * type of the actions of the screen, its `when (action)` must handle all of them.
     */
    inline fun <reified F : EmaAction> addReducer(noinline reduceAction: ReducerScope<S>.(F) -> S) {
        addReducer(F::class, reduceAction)
    }

    /**
     * Adds side effects. Each one runs asynchronously after the reducers of the actions of its type.
     */
    fun addSideEffect(vararg sideEffect: SideEffect<S, A, E, *>) {
        checkNotBuilt()
        sideEffectList = sideEffectList + sideEffect
    }

    /**
     * Implementation of the inline addSideEffect with the type of the actions.
     */
    @PublishedApi
    internal fun <F : EmaAction> addSideEffect(
        filter: KClass<F>,
        concurrency: SideEffectConcurrency,
        timeout: Duration?,
        onTimeout: (SideEffectScope<S, A, E>.(F) -> Unit)?,
        sideEffect: suspend SideEffectScope<S, A, E>.(F) -> Unit
    ) {
        addSideEffect(sideEffectOf(filter, concurrency, timeout, onTimeout, sideEffect))
    }

    /**
     * Adds a side effect for the actions of type [F]. It runs asynchronously after their reducers.
     * @param concurrency what happens when an action launches it while it is still running
     * @param timeout if the side effect doesn't finish in this time, it is cancelled and [onTimeout] is called
     */
    inline fun <reified F : EmaAction> addSideEffect(
        concurrency: SideEffectConcurrency = SideEffectConcurrency.Every,
        timeout: Duration? = null,
        noinline onTimeout: (SideEffectScope<S, A, E>.(F) -> Unit)? = null,
        noinline sideEffect: suspend SideEffectScope<S, A, E>.(F) -> Unit
    ) {
        addSideEffect(F::class, concurrency, timeout, onTimeout, sideEffect)
    }

    /**
     * Implementation of the inline addBroadcastListener with the type of the actions.
     */
    @PublishedApi
    internal fun addBroadcastListener(actionType: KClass<out EmaAction>) {
        checkNotBuilt()
        broadcastListenerList = broadcastListenerList + actionType
    }

    /**
     * Listens to the actions of type [F] broadcast with the broadcast function of any side effect. The type of the
     * action is the id of the broadcast. They enter this source like the dispatched ones: they go through its
     * middlewares, reducers and side effects of type [F]. The listener is removed when the source is closed.
     */
    inline fun <reified F : EmaAction> addBroadcastListener() {
        addBroadcastListener(F::class)
    }

    /**
     * Called when setup finishes. Nothing can be added after it.
     */
    internal fun build() {
        built = true
    }

    private fun checkNotBuilt() {
        check(!built) { "The source can only be configured inside setup" }
    }
}
