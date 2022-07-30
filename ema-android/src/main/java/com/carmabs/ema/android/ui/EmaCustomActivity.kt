package com.carmabs.ema.android.ui

import androidx.viewbinding.ViewBinding
import com.carmabs.ema.core.navigator.EmaNavigationTarget
import com.carmabs.ema.core.state.EmaBaseState
import com.carmabs.ema.core.viewmodel.EmaViewModel

/**
 *
 * Base activity to bind and unbind view model
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

abstract class EmaCustomActivity<B:ViewBinding,S : EmaBaseState, VM : EmaViewModel<S, NS>, NS : EmaNavigationTarget> :
    EmaToolbarFragmentActivity<B, S, VM, NS>()