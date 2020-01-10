package com.carmabs.ema.presentation.ui.home

import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.presentation.base.BaseViewModel

/**
 * Project: Ema
 * Created by: cmateob on 20/1/19.
 */
class EmaHomeToolbarViewModel : BaseViewModel<EmaHomeToolbarState, EmaHomeNavigator.Navigation>() {


    override fun onStartFirstTime(statePreloaded: Boolean) {

    }

    override fun onResultListenerSetup() {
       addOnResultReceived{
           (it.data as? Pair<*, *>)?.also { pair ->
               sendSingleEvent(EmaExtraData(extraData = pair))
           }
       }
    }

    override fun createInitialViewState(): EmaHomeToolbarState = EmaHomeToolbarState()
}