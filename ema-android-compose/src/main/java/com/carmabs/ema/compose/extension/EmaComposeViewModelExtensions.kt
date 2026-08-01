@file:Suppress("UNCHECKED_CAST")

package com.carmabs.ema.compose.extension

import com.carmabs.ema.core.action.EmaAction
import com.carmabs.ema.core.action.EmaActionDispatcher
import com.carmabs.ema.core.state.EmaEvent
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.viewmodel.EmaViewModel
import com.carmabs.ema.core.viewmodel.EmaViewModelAction

/**
 * Created by Carlos Mateo Benito on 12/9/23.
 *
 * <p>
 * Copyright (c) 2023 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
fun <S : EmaState, A : EmaAction.Screen, E : EmaEvent> EmaViewModel<S, E>.asViewModelAction(): EmaViewModelAction<S, A, E> {
    return (this as? EmaViewModelAction<S, A, E>)
        ?: throw java.lang.IllegalStateException("${this::class} must inherit form EmaViewModelAction class")
}

fun <A : EmaAction.Screen> EmaViewModel<*, *>.asActionDispatcher(): EmaActionDispatcher<A> {
    return (this as? EmaActionDispatcher<A>)
        ?: throw java.lang.IllegalStateException("${this::class} must implement EmaActionDispatcher with the proper action")
}
