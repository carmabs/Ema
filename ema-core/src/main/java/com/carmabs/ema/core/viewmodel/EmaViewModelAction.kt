package com.carmabs.ema.core.viewmodel

import com.carmabs.ema.core.action.EmaAction
import com.carmabs.ema.core.action.EmaActionDispatcher
import com.carmabs.ema.core.concurrency.EmaMainScope
import com.carmabs.ema.core.state.EmaEvent
import com.carmabs.ema.core.state.EmaState
import kotlinx.coroutines.CoroutineScope

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
abstract class EmaViewModelAction<S : EmaState, A : EmaAction.Screen, E: EmaEvent>(
    initialDataState: S,
    scope: CoroutineScope = EmaMainScope()
) : EmaViewModelBasic<S,E>(initialDataState, scope), EmaActionDispatcher<A> {

    final override fun dispatch(action: A) {
        onAction(action)
    }

    protected abstract fun onAction(action: A)
}
