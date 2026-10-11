package com.carmabs.emanate.presentation.base

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import com.carmabs.emanate.core.viewmodel.EmaViewModelAction

/**
 * Base of the view models built with EmaViewModelAction: they handle each action imperatively in onAction.
 *
 * <p>
 * Copyright (c) 2020, Carmabs. All rights reserved.
 * </p>
 *
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
abstract class BaseActionViewModel<S : EmaState, A : EmaAction, E : EmaEvent>(initialDataState: S) :
    EmaViewModelAction<S, A, E>(initialDataState),
    SampleViewModel<S, A, E>
