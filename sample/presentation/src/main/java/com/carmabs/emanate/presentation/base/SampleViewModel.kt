package com.carmabs.emanate.presentation.base

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.action.EmaActionDispatcher
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import com.carmabs.emanate.core.viewmodel.EmaViewModelContract

/**
 * View model as the screens see it: they render its state, consume its events and dispatch its actions, whatever
 * its implementation is.
 *
 * <p>
 * Copyright (c) 2026, Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
interface SampleViewModel<S : EmaState, A : EmaAction, E : EmaEvent> :
    EmaViewModelContract<S, E>,
    EmaActionDispatcher<A>
