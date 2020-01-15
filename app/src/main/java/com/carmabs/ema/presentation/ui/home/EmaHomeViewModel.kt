package com.carmabs.ema.presentation.ui.home

import com.carmabs.domain.exception.UserEmptyException
import com.carmabs.domain.manager.ResourceManager
import com.carmabs.domain.model.LoginRequest
import com.carmabs.domain.usecase.LoginUseCase
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
                sendSingleEvent(EmaExtraData(extraData = pair))
            }
        }

        */
    }



    override val initialViewState: EmaHomeState = EmaHomeState()

    private fun doLogin() {
        checkViewState {
            executeUseCaseWithException(
                    {
                        loading()
                        val user = loginUseCase.execute(LoginRequest(it.userName, it.userPassword))
                        updateViewState()
                        sendSingleEvent(EmaExtraData(EVENT_MESSAGE, resourceManager.getCongratulations()))
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
                        notifyError(e)
                        navigate(EmaHomeNavigator.Navigation.Error)
                    }
            )
        }
    }

    fun onActionLogin() {
        checkViewState {
            when {
                it.userName.isEmpty() -> notifyError(UserEmptyException())
                it.userPassword.isEmpty() -> notifyError(UserEmptyException())
                else -> doLogin()
            }
        }
    }

    fun onActionShowPassword() {
        updateViewState {
            copy(showPassword = !showPassword)
        }
    }

    fun onActionRemember(isChecked: Boolean) {
        updateViewState(false) {
            copy(rememberUser = isChecked)
        }
    }

    fun onActionDeletePassword() {
        updateViewState {
            copy(userPassword = "")
        }
    }

    fun onActionDeleteUser() {
        updateViewState {
            copy(userName = "")
        }
    }

    fun onActionPasswordWrite(password: String) {
        updateViewState(false) {
            copy(userPassword = password)
        }
    }

    fun onActionUserWrite(user: String) {
        updateViewState(false) {
            copy(userName = user)
        }
    }

    fun onActionDialogErrorCancel() {
        updateViewState()
    }

    fun onActionDialogErrorAccept() {
        updateViewState()
    }
}