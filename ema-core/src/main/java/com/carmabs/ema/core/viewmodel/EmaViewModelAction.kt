package com.carmabs.ema.core.viewmodel

import com.carmabs.ema.core.action.EmaAction
import com.carmabs.ema.core.action.EmaActionDispatcher
import com.carmabs.ema.core.concurrency.EmaMainScope
import com.carmabs.ema.core.model.emaFlowSingleEvent
import com.carmabs.ema.core.state.EmaEffect
import com.carmabs.ema.core.state.EmaState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

/**
 * Created by Carlos Mateo Benito on 14/04/2023.
 *
 * Viewmodel that handles actions by the method onAction(action:EmaAction).
 * It centralize the user actions on view.Based on MVI
 * <p>
 * Copyright (c) 2023 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
abstract class EmaViewModelAction<S : EmaState, A : EmaAction.Screen, E: EmaEffect>(
    initialDataState: S,
    scope: CoroutineScope = EmaMainScope()
) : EmaViewModelBasic<S,E>(initialDataState, scope), EmaActionDispatcher<A> {

    private val actionFlow = emaFlowSingleEvent<A>()

    /**
     * Subscribe to the actions dispatched to this ViewModel
     */
    final override fun subscribeToActions(): Flow<A> = actionFlow

    final override fun dispatch(action: A) {
        onAction(action)
        actionFlow.tryEmit(action)
    }

    abstract fun onAction(action: A)
}
