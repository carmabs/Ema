package com.carmabs.ema.presentation.ui.error

import com.carmabs.ema.android.viewmodel.EmaViewModel
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.presentation.ui.backdata.userlist.EmaBackUserViewModel


class EmaErrorViewModel : EmaViewModel<EmaErrorState, EmaErrorNavigator.Navigation>() {

    lateinit var toolbarViewModel: EmaErrorToolbarViewModel

    override fun onStartFirstTime(statePreloaded: Boolean) {

    }

    override fun onResultListenerSetup() {
       /* addOnResultReceived(EmaBackUserViewModel.RESULT_USER_NUMBER){
            sendSingleEvent(EmaExtraData(extraData = it.data as Int))
        }*/
    }

    override fun createInitialViewState(): EmaErrorState = EmaErrorState()

    fun onActionToolbar() {
        updateViewState { copy(showToolbarViewVisibility = false) }
        toolbarViewModel.showToolbar()
    }

    fun onToolbarUpdated(toolbarState: EmaState<EmaErrorToolbarState>) {
        when (toolbarState) {
            is EmaState.Normal -> {
                updateViewState {
                    copy(showToolbarViewVisibility = !toolbarState.data.visibility)
                }
            }
            is EmaState.Loading -> { /* HANDLE LOADING STATE*/
            }
            is EmaState.Error -> { /*HANDLE ERROR STATE*/
            }
        }
    }

    fun onActionAddUser() {
        navigate(EmaErrorNavigator.Navigation.BackUser)
    }

}