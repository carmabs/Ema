package com.carmabs.emanate.core.viewmodel

import com.carmabs.emanate.core.Emanate
import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.action.EmaActionDispatcher
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import kotlinx.coroutines.CoroutineDispatcher

/**
 * Created by Carlos Mateo Benito on 14/04/2023.
 *
 * Viewmodel that handles actions by the method onAction(action:EmaAction).
 * It centralize the user actions on view.Based on MVI
 * @param dispatcher where the view model runs. By default the main dispatcher of Emanate.
 * <p>
 * Copyright (c) 2023 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
abstract class EmaViewModelAction<S : EmaState, A : EmaAction, E : EmaEvent>(
    initialDataState: S,
    dispatcher: CoroutineDispatcher = Emanate.configuration.dispatchers.main
) : EmaViewModelClassic<S, E>(initialDataState, dispatcher),
    EmaActionDispatcher<A> {

    final override fun dispatch(action: A) {
        onAction(action)
    }

    protected abstract fun onAction(action: A)
}
