package com.carmabs.emanate.core.source.reducer

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.source.EmaSourceDsl
import com.carmabs.emanate.core.source.SetupGuard
import com.carmabs.emanate.core.state.EmaState
import kotlin.reflect.KClass

/**
 * Created by Carlos Mateo Benito on 12/10/2026.
 *
 * Block of setup that adds the reducers of the source: with [add], or with `+` for the ones already created. Each one
 * only receives the actions of its type, in the order they are added.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
@EmaSourceDsl
class ReducerListScope<S : EmaState> internal constructor(private val guard: SetupGuard) {

    internal var reducerList: List<EmaReducer<S, *>> = emptyList()
        private set

    /**
     * Adds reducers already created.
     */
    fun add(vararg reducer: EmaReducer<S, *>) {
        guard.checkNotBuilt()
        reducerList = reducerList + reducer
    }

    /**
     * Adds a reducer already created.
     */
    operator fun EmaReducer<S, *>.unaryPlus() {
        add(this)
    }

    /**
     * Implementation of the inline add with the type of the actions.
     */
    @PublishedApi
    internal fun <F : EmaAction> add(filter: KClass<F>, reduceAction: ReducerScope<S>.(F) -> S) {
        add(reducerOf(filter, reduceAction))
    }

    /**
     * Adds a reducer for the actions of type [F]: of the source, initializers or lifecycle actions. With the sealed
     * type of the actions of the screen, its `when (action)` must handle all of them.
     */
    inline fun <reified F : EmaAction> add(noinline reduceAction: ReducerScope<S>.(F) -> S) {
        add(F::class, reduceAction)
    }
}
