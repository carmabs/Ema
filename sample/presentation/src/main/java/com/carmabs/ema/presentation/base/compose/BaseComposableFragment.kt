package com.carmabs.ema.presentation.base.compose

import com.carmabs.ema.compose.ui.EmaComposableFragment
import com.carmabs.ema.core.state.EmaEvent
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.viewmodel.EmaViewModel


/**
 *  *<p>
 * Copyright (c) 2020, Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

abstract class BaseComposableFragment<S : EmaState, VM : EmaViewModel<S, E>, E : EmaEvent> :
    EmaComposableFragment<S, VM, E>()
