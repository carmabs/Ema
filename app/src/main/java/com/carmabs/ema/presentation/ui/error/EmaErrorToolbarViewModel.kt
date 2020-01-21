package com.carmabs.ema.presentation.ui.error

import com.carmabs.ema.android.viewmodel.EmaViewModel
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.presentation.ui.backdata.userlist.EmaBackUserViewModel

/**
 *
 *
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

class EmaErrorToolbarViewModel : EmaViewModel<EmaErrorToolbarState, EmaErrorNavigator.Navigation>() {

    override val initialViewState: EmaErrorToolbarState = EmaErrorToolbarState()

    override fun onStartFirstTime(statePreloaded: Boolean) {

    }

    override fun onResultListenerSetup() {
        addOnResultReceived(EmaBackUserViewModel.RESULT_USER_NUMBER) {
            notifySingleEvent(EmaExtraData(extraData = it.data as Int))
        }
    }

    fun onActionMenuHideToolbar() {
        hideToolbar()
    }

    fun showToolbar() {
        updateNormalState {
            copy(visibility = true)
        }
    }

    fun hideToolbar() {
        updateNormalState {
            copy(visibility = false)
        }
    }
}