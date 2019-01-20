package com.carmabs.ema.presentation.ui.emaway.user

import com.carmabs.ema.android.viewmodel.EmaViewModel
import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.ema.presentation.ui.emaway.user.EmaUserState

class EmaUserViewModel : EmaViewModel<EmaUserState,EmaNavigationState>() {

    override fun createInitialViewState(): EmaUserState {
       return EmaUserState()
    }
}
