package com.carmabs.ema.presentation.base

import com.carmabs.ema.android.ui.EmaFragment
import com.carmabs.ema.android.viewmodel.EmaViewModel
import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.ema.core.state.EmaBaseState
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.presentation.injection.fragmentInjection
import org.kodein.di.Kodein

/**
 * TODO: Add a class header comment.
 *
*
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

abstract class BaseFragment<S : EmaBaseState, VM : EmaViewModel<S, NS>, NS : EmaNavigationState> : EmaFragment<S,VM,NS>() {

    override val fragmentViewModelScope: Boolean = true

    override fun injectFragmentModule(kodein: Kodein.MainBuilder): Kodein.Module?  = fragmentInjection(this)

    override fun onStateNormal(data: S) {
        onNormal(data)
    }

    override fun onStateAlternative(data: EmaExtraData) {
        onAlternative(data)
    }

    override fun onSingleEvent(data: EmaExtraData) {
        onSingle(data)
    }

    override fun onStateError(error: Throwable) {
        onError(error)
    }

    abstract fun onNormal(data: S)

    abstract fun onAlternative(data: EmaExtraData)

    abstract fun onSingle(data: EmaExtraData)

    abstract fun onError(error: Throwable)

}