package com.carmabs.ema.presentation.ui.error

import com.carmabs.ema.android.viewmodel.EmaViewModel
import com.carmabs.ema.core.state.EmaState


class EmaErrorViewModel : EmaViewModel<EmaErrorState, EmaErrorNavigator.Navigation>() {

    lateinit var toolbarViewModel: EmaErrorToolbarViewModel

    override fun onStartFirstTime(statePreloaded: Boolean) {

    }

    override val initialViewState: EmaErrorState = EmaErrorState()

    fun onActionToolbar() {
        updateNormalState { copy(showToolbarViewVisibility = false) }
        toolbarViewModel.showToolbar()
    }

    fun onToolbarUpdated(toolbarState: EmaState<EmaErrorToolbarState>) {
        when (toolbarState) {
            is EmaState.Normal -> {
                updateNormalState {
                    copy(showToolbarViewVisibility = !toolbarState.data.visibility)
                }
            }
            is EmaState.Alternative -> { /* HANDLE LOADING STATE*/
            }
            is EmaState.Error -> { /*HANDLE ERROR STATE*/
            }
        }
    }

    fun onActionAddUser() {
        navigate(EmaErrorNavigator.Navigation.BackUser)
    }
}