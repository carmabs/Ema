package com.carmabs.ema.presentation.ui.home

import com.carmabs.domain.exception.UserEmptyException
import com.carmabs.domain.manager.ResourceManager
import com.carmabs.domain.model.LoginRequest
import com.carmabs.domain.usecase.LoginUseCase
import com.carmabs.ema.core.constants.STRING_EMPTY
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.presentation.base.BaseViewModel
import com.carmabs.ema.presentation.ui.user.EmaUserState

/**
 *  *<p>
 * Copyright (c) 2020, Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 *
 * Created by: Carlos Mateo Benito on 20/1/19.
 */
class EmaHomeViewModel(
        private val loginUseCase: LoginUseCase,
        private val resourceManager: ResourceManager
) : BaseViewModel<EmaHomeState, EmaHomeNavigator.Navigation>() {

    companion object {
        const val EVENT_MESSAGE = 1000
    }

    override fun onStartFirstTime(statePreloaded: Boolean) {

    }

    override fun onResultListenerSetup() {
        //When two or more resultReceived WITH THE SAME CODE are active, for example in this case,
        //this receiver and the EmaHomeToolbarViewModel receiver, only the last one is executed.
        //ActivityCreated -> EmaHomeToolbarViewModel added -> Fragment created -> EEmaHomeViewModel added->
        //only this result received is executed.


        /* Uncomment to test it

        addOnResultReceived{
            (it.data as? Pair<*, *>)?.also { pair ->
                notifySingleEvent(EmaExtraData(extraData = pair))
            }
        }

        */
    }


    override val initialViewState: EmaHomeState = EmaHomeState()

    private fun doLogin() {
        checkDataState {
            executeUseCaseWithException(
                    {
                        updateAlternativeState()
                        val user = loginUseCase.execute(LoginRequest(it.userName, it.userPassword))
                        updateNormalState()
                        notifySingleEvent(EmaExtraData(EVENT_MESSAGE, resourceManager.getCongratulations()))
                        navigate(
                                EmaHomeNavigator.Navigation.User(
                                        EmaUserState(
                                                name = user.name,
                                                surname = user.surname
                                        )
                                )
                        )
                    },
                    { e ->
                        updateErrorState(e)
                        navigate(EmaHomeNavigator.Navigation.Error)
                    }
            )
        }
    }

    fun onActionLogin() {
        checkDataState {
            when {
                it.userName.isEmpty() -> updateErrorState(UserEmptyException())
                it.userPassword.isEmpty() -> updateErrorState(UserEmptyException())
                else -> doLogin()
            }
        }
    }

    fun onActionShowPassword() {
        updateNormalState {
            copy(showPassword = !showPassword)
        }
    }

    fun onActionRemember(isChecked: Boolean) {
        updateNormalState {
            copy(rememberUser = isChecked)
        }
    }

    fun onActionDeletePassword() {
        updateNormalState {
            copy(userPassword = STRING_EMPTY)
        }
    }

    fun onActionDeleteUser() {
        updateNormalState(false) {
            copy(userName = STRING_EMPTY)
        }
    }


    fun onActionUserWrite(user: String) {
        //We only want to update the data of the view without notifying it, it has the edit text updated with
        //text when you write on it, but you need to save the state if for example, there is a device
        //rotation and the view is recreated, to set the text with last value saved on state

        updateDataState {
            copy(userName = user)
        }
    }

    fun onActionPasswordWrite(password: String) {
        updateDataState {
            copy(userPassword = password)
        }
    }

    fun onActionDialogErrorCancel() {
        updateNormalState()
    }

    fun onActionDialogErrorAccept() {
        updateNormalState()
    }
}