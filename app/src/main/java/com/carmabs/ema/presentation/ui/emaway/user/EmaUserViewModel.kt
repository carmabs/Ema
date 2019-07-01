package com.carmabs.ema.presentation.ui.emaway.user

import com.carmabs.ema.android.viewmodel.EmaViewModel
import com.carmabs.ema.core.navigator.EmaNavigationState

class EmaUserViewModel : EmaViewModel<EmaUserState,EmaNavigationState>() {

    override fun onStartFirstTime(statePreloaded: Boolean) {

    }


    override fun createInitialViewState(): EmaUserState {
       return EmaUserState()
    }
}
