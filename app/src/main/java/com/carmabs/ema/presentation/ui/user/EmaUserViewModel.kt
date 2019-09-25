package com.carmabs.ema.presentation.ui.user

import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.ema.presentation.base.BaseViewModel

class EmaUserViewModel : BaseViewModel<EmaUserState, EmaNavigationState>() {

    override fun onStartFirstTime(statePreloaded: Boolean) {
       val list =  createListItems()
        updateViewState {
            copy(itemList = list)
        }
    }

    private fun createListItems():List<EmaUserItemModel> {
        return listOf(
                EmaUserRightModel(1),
                EmaUserLeftModel("Item 2"),
                EmaUserLeftModel("Item 3"),
                EmaUserRightModel(4)
        )
    }


    override fun createInitialViewState(): EmaUserState {
       return EmaUserState()
    }
}
