package com.carmabs.emanate.presentation.base

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import com.carmabs.emanate.core.viewmodel.EmaViewModel

/**
 * Base of the view models built with EmaViewModel: they declare in setup the reducers, middlewares and side effects
 * of each action.
 *
 * <p>
 * Copyright (c) 2026, Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
abstract class BaseViewModel<S : EmaState, A : EmaAction, E : EmaEvent>(initialDataState: S) :
    EmaViewModel<S, A, E>(initialDataState),
    SampleViewModel<S, A, E>
