package com.carmabs.ema.presentation.ui.error

import com.carmabs.ema.android.viewmodel.EmaViewModel
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.presentation.ui.home.EmaHomeNavigator


class EmaErrorViewModel : EmaViewModel<EmaErrorState, EmaHomeNavigator.Navigation>() {

    lateinit var toolbarViewModel: EmaToolbarViewModel

    override fun onStartFirstTime(statePreloaded: Boolean) {

    }

    override fun createInitialViewState(): EmaErrorState = EmaErrorState()

    fun onActionToolbar() {
        updateViewState { copy(showToolbarViewVisibility = false) }
        toolbarViewModel.showToolbar()
    }

    fun onToolbarUpdated(toolbarState: EmaState<EmaToolbarState>) {
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

}