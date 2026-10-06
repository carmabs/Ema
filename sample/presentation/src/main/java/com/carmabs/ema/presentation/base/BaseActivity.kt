package com.carmabs.ema.presentation.base

import androidx.viewbinding.ViewBinding
import com.carmabs.ema.android.ui.EmaActivity
import com.carmabs.ema.core.state.EmaEvent
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.viewmodel.EmaViewModel

/**
 * Base Activity. OverrideTheme -> True, the theme is overriden by AppTheme
 * Injection is provided
 *
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

abstract class BaseActivity<B : ViewBinding, S : EmaState, VM : EmaViewModel<S, E>, E : EmaEvent> :
    EmaActivity<B, S, VM, E>()
