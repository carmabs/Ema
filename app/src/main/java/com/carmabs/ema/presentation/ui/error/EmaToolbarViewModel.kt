package com.carmabs.ema.presentation.ui.error

import com.carmabs.ema.android.viewmodel.EmaViewModel
import com.carmabs.ema.presentation.ui.home.EmaHomeNavigator

/**
 *
*
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

class EmaToolbarViewModel : EmaViewModel<EmaToolbarState, EmaHomeNavigator.Navigation>() {

    override fun createInitialViewState(): EmaToolbarState = EmaToolbarState()

    override fun onStartFirstTime(statePreloaded: Boolean) {

    }

    fun onActionMenuHideToolbar() {
        hideToolbar()
    }

    fun showToolbar(){
        updateViewState{
            copy(visibility = true)
        }
    }

    fun hideToolbar(){
        updateViewState{
            copy(visibility = false)
        }
    }
}