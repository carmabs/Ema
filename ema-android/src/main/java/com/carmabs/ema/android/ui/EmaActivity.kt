package com.carmabs.ema.android.ui

import android.view.LayoutInflater
import com.carmabs.ema.android.databinding.EmaToolbarActivityBinding
import com.carmabs.ema.core.navigator.EmaNavigationTarget
import com.carmabs.ema.core.state.EmaBaseState
import com.carmabs.ema.core.viewmodel.EmaViewModel

/**
 *
 * Base activity to bind and unbind view model
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

abstract class EmaActivity<S : EmaBaseState, VM : EmaViewModel<S, NS>, NS : EmaNavigationTarget> :
    EmaToolbarFragmentActivity<EmaToolbarActivityBinding, S, VM, NS>(){

    override fun createViewBinding(inflater: LayoutInflater): EmaToolbarActivityBinding {
        return EmaToolbarActivityBinding.inflate(inflater)
    }
}