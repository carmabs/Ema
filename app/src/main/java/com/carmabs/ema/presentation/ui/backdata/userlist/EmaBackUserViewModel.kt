package com.carmabs.ema.presentation.ui.backdata.userlist;

import com.carmabs.ema.android.viewmodel.EmaViewModel
import com.carmabs.ema.presentation.ui.backdata.EmaBackNavigator
import com.carmabs.ema.presentation.ui.backdata.creation.EmaBackUserCreationViewModel

/**
 * <p>
 * Copyright (c) 2019, Babel Sistemas de Información. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:carlos.mateo@babel.es”>Carlos Mateo</a>
 * <p>
 * Date: 2019-11-07
 */

class EmaBackUserViewModel : EmaViewModel<EmaBackUserState, EmaBackNavigator.Navigation>() {

    companion object {
        const val RESULT_USER_NUMBER = 1000
    }
    override fun createInitialViewState(): EmaBackUserState {
        return EmaBackUserState()
    }

    override fun onStartFirstTime(statePreloaded: Boolean) {

    }

    override fun onResultListenerSetup() {
        addOnResultReceived(EmaBackUserCreationViewModel.RESULT_USER){
            updateViewState {
                val mutableList = listUsers.toMutableList()
                mutableList.add(it.data as EmaBackUserModel)
                addResult(mutableList.size,RESULT_USER_NUMBER)
                copy(listUsers = mutableList,noUserVisibility = mutableList.isEmpty())
            }
        }
    }

    fun onActionAddUser() {
        navigate(EmaBackNavigator.Navigation.Result)
    }


}
