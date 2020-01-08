package com.carmabs.ema.presentation.ui.home

import com.carmabs.ema.presentation.base.BaseViewModel
import com.carmabs.ema.presentation.ui.backdata.creation.EmaBackUserCreationViewModel

/**
 * Project: Ema
 * Created by: cmateob on 20/1/19.
 */
class EmaHomeToolbarViewModel : BaseViewModel<EmaHomeToolbarState, EmaHomeNavigator.Navigation>() {


    override fun onStartFirstTime(statePreloaded: Boolean) {

    }

    override fun onResultListenerSetup() {
       addOnResultReceived(EmaBackUserCreationViewModel.RESULT_USER){
           val p = it
       }
    }

    override fun createInitialViewState(): EmaHomeToolbarState = EmaHomeToolbarState()
}