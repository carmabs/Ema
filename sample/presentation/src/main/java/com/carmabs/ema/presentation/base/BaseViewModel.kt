package com.carmabs.ema.presentation.base

import com.carmabs.ema.core.action.EmaAction
import com.carmabs.ema.core.state.EmaEvent
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.viewmodel.EmaViewModelAction

/**
 *  *<p>
 * Copyright (c) 2020, Carmabs. All rights reserved.
 * </p>
 *
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

abstract class BaseViewModel<S : EmaState, A : EmaAction.Screen, E : EmaEvent>(initialDataState: S) :
    EmaViewModelAction<S, A, E>(initialDataState)
