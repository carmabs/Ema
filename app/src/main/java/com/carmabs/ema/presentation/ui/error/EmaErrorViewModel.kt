package com.carmabs.ema.presentation.ui.error

import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.presentation.base.BaseViewModel


class EmaErrorViewModel : BaseViewModel<EmaErrorState, EmaErrorNavigator.Navigation>() {

    lateinit var toolbarViewModel: EmaErrorToolbarViewModel

    override fun onStartFirstTime(statePreloaded: Boolean) {

    }

    override val initialViewState: EmaErrorState = EmaErrorState()

    fun onActionToolbar() {
        updateToNormalState { copy(showToolbarViewVisibility = false) }
        toolbarViewModel.showToolbar()
    }

    fun onToolbarUpdated(toolbarState: EmaState<EmaErrorToolbarState>) {
        when (toolbarState) {
            is EmaState.Normal -> {
                updateToNormalState {
                    copy(showToolbarViewVisibility = !toolbarState.data.visibility)
                }
            }
            is EmaState.Overlayed -> { /* HANDLE LOADING STATE*/
            }
            is EmaState.Error -> { /*HANDLE ERROR STATE*/
            }
        }
    }

    fun onActionAddUser() {
        navigate(EmaErrorNavigator.Navigation.BackUser)
    }
}