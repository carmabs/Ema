package com.carmabs.emanate.core.source

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.source.reducer.EmaReducer
import com.carmabs.emanate.core.source.reducer.ReducerScope
import com.carmabs.emanate.core.source.reducer.reducerOf
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import kotlin.reflect.KClass

/**
 * Created by Carlos Mateo Benito on 11/10/2026.
 *
 * Adds the reducers of the source. It can only be used inside setup.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
@EmaDsl
class SourceSetupScope<S : EmaState, A : EmaAction, E : EmaEvent> internal constructor() {

    internal var reducerList: List<EmaReducer<S, *>> = emptyList()
        private set

    private var built = false

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
     * Called when setup finishes. Nothing can be added after it.
     */
    internal fun build() {
        built = true
    }

    private fun checkNotBuilt() {
        check(!built) { "The source can only be configured inside setup" }
    }
}
