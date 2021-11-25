package com.carmabs.ema.presentation.ui.unlogged

import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.presentation.base.BaseViewModel
import kotlin.random.Random


class EmaUnloggedViewModel : BaseViewModel<EmaUnloggedState, EmaUnloggedNavigator.Navigation>() {

    lateinit var toolbarViewModel: EmaUnloggedToolbarViewModel

    companion object{
        const val EXTRA_DATA_SHOW_DIALOG = 0
    }

    override fun onStartFirstTime(statePreloaded: Boolean) {

    }

    override val initialViewState: EmaUnloggedState = EmaUnloggedState()

    fun onActionToolbar() {
        updateToNormalState { copy(showToolbarViewVisibility = false) }
        toolbarViewModel.showToolbar()
    }

    fun onToolbarUpdated(toolbarState: EmaState<EmaUnloggedToolbarState>) {
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
        navigate(EmaUnloggedNavigator.Navigation.BackUser)
    }

    fun onActionSingleEvent() {
        notifySingleEvent(EmaExtraData(2, Random.nextInt().toString()))

    }

    fun onActionShowDialog() {
        updateToAlternativeState(EmaExtraData(EXTRA_DATA_SHOW_DIALOG,"Test Dialog String"))
    }

    fun onActionHideDialog() {
        updateToNormalState()
    }
}