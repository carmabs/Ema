package com.carmabs.ema.presentation.ui.home

import com.carmabs.domain.exception.UserEmptyException
import com.carmabs.domain.model.LoginRequest
import com.carmabs.domain.usecase.LoginUseCase
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.presentation.base.BaseViewModel
import com.carmabs.ema.presentation.ui.backdata.EmaBackToolbarViewModel
import com.carmabs.ema.presentation.ui.backdata.userlist.EmaBackUserViewModel
import com.carmabs.ema.presentation.ui.error.EmaErrorToolbarViewModel
import com.carmabs.ema.presentation.ui.error.EmaErrorViewModel

/**
 * Project: Ema
 * Created by: cmateob on 20/1/19.
 */
class EmaHomeViewModel(private val loginUseCase: LoginUseCase) : BaseViewModel<EmaHomeState, EmaHomeNavigator.Navigation>() {

    companion object{
        const val EVENT_MESSAGE = 1000
    }

    override fun onStartFirstTime(statePreloaded: Boolean) {

    }

    override fun createInitialViewState(): EmaHomeState = EmaHomeState()

    private fun doLogin() {
        checkViewState {
            executeUseCaseWithException(
                    {
                        loading()
                        val user = loginUseCase.doLogin(LoginRequest(it.userName, it.userPassword))
                        updateViewState()
                        sendSingleEvent(EmaExtraData(EVENT_MESSAGE,"Congratulations"))
                        setResult(0,1)
                        navigate(EmaHomeNavigator.Navigation.User(user))
                    },
                    {
                        e -> notifyError(e)
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

    fun onActionRemember(isChecked:Boolean) {
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