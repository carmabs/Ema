package com.carmabs.ema.presentation.ui.emaway.home

import com.carmabs.ema.android.viewmodel.EmaViewModel
import com.carmabs.ema.domain.exception.UserEmptyException
import com.carmabs.ema.domain.model.LoginRequest
import com.carmabs.ema.domain.usecase.LoginUseCase

/**
 * Project: Ema
 * Created by: cmateob on 20/1/19.
 */
class EmaHomeViewModel(private val loginUseCase: LoginUseCase) : EmaViewModel<EmaHomeState, EmaHomeNavigator.Navigation>() {

    override fun onStartFirstTime(statePreloaded: Boolean) {

    }

    override fun createInitialViewState(): EmaHomeState = EmaHomeState()

    private fun doLogin() {
        viewState?.let {
            executeUseCaseWithException(
                    {
                        loading()
                        val user = loginUseCase.doLogin(LoginRequest(it.userName, it.userPassword))
                        updateViewState()
                        navigate(EmaHomeNavigator.Navigation.User(user))
                    },
                    { e -> notifyError(e) }
            )
        }
    }

    fun onActionLogin() {
        viewState?.let {
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
            copy(rememberuser = isChecked)
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