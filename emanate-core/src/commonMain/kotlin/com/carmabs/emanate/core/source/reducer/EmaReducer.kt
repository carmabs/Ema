package com.carmabs.emanate.core.source.reducer

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.source.SourceSetupScope
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import com.carmabs.emanate.core.viewmodel.EmaViewModel
import kotlin.reflect.KClass

/**
 * Created by Carlos Mateo Benito on 11/10/2026.
 *
 * Reducer for the actions of type [F]. The other actions keep the state.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
interface EmaReducer<S : EmaState, F : EmaAction> {

    /**
     * Type of the actions it reduces. With the sealed type of the actions of the screen, `when (action)` must
     * handle all of them.
     */
    val actionType: KClass<F>

    /**
     * Returns the new state for [action]. The current state is [ReducerScope.state].
     */
    fun ReducerScope<S>.reduce(action: F): S
}

fun <S : EmaState, F : EmaAction> reducerOf(
    actionType: KClass<F>,
    reduceAction: ReducerScope<S>.(F) -> S
): EmaReducer<S, F> = object : EmaReducer<S, F> {

    override val actionType: KClass<F> = actionType

    override fun ReducerScope<S>.reduce(action: F): S = reduceAction(action)
}

fun <S : EmaState, A : EmaAction, E : EmaEvent, F : EmaAction> SourceSetupScope<S, A, E>.reducerOf(
    actionType: KClass<F>,
    reduceAction: ReducerScope<S>.(F) -> S
): EmaReducer<S, F> = com.carmabs.emanate.core.source.reducer.reducerOf(actionType, reduceAction)

fun <S : EmaState, A : EmaAction, E : EmaEvent, F : EmaAction> EmaViewModel<S, A, E>.reducerOf(
    actionType: KClass<F>,
    reduceAction: ReducerScope<S>.(F) -> S
): EmaReducer<S, F> = com.carmabs.emanate.core.source.reducer.reducerOf(actionType, reduceAction)
