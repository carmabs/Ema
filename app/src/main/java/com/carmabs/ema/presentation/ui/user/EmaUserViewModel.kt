package com.carmabs.ema.presentation.ui.user

import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.presentation.base.BaseViewModel

class EmaUserViewModel : BaseViewModel<EmaUserState, EmaNavigationState>() {

    companion object{
        const val SINGLE_EVENT_GROUP = 0
        const val SINGLE_EVENT_USER = 1
    }

    override fun onStartFirstTime(statePreloaded: Boolean) {
       val list =  createListItems()
        updateViewState {
            copy(itemList = list)
        }
    }

    private fun createListItems():List<EmaUserItemModel> {
        return listOf(
                EmaUserRightModel(3),
                EmaUserLeftModel("Carmabs"),
                EmaUserLeftModel("EMA"),
                EmaUserRightModel(6)
        )
    }


    override fun createInitialViewState(): EmaUserState {
       return EmaUserState()
    }

    fun onActionUserClicked(item: EmaUserItemModel) {
        val eventID = when(item.type){
            EmaUserItemModel.Type.LEFT -> SINGLE_EVENT_USER
            EmaUserItemModel.Type.RIGHT -> SINGLE_EVENT_GROUP
        }
        sendSingleEvent(EmaExtraData(eventID,item))
    }
}
