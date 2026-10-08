package com.carmabs.ema.presentation.login

import com.carmabs.domain.model.User
import com.carmabs.domain.usecase.LoginUseCase
import com.carmabs.ema.core.action.EmaAction
import com.carmabs.ema.core.broadcast.backBroadcastId
import com.carmabs.ema.core.constants.STRING_EMPTY
import com.carmabs.ema.core.model.onFailure
import com.carmabs.ema.core.model.onSuccess
import com.carmabs.ema.presentation.base.BaseViewModel
import com.carmabs.ema.presentation.home.HomeViewModel

class LoginViewModel(private val loginUseCase: LoginUseCase, initialDataState: LoginState) :
    BaseViewModel<LoginState, LoginAction, LoginEvent>(initialDataState) {
    override fun onStateCreated(initializer: EmaAction.Initializer?) = Unit
    override fun onAction(action: LoginAction) {
        when (action) {
            LoginAction.DeleteUser -> {
                onActionDeleteUser()
            }

            LoginAction.Login -> {
                onActionLogin()
            }

            is LoginAction.PasswordWritten -> {
                onActionPasswordWrite(action.password)
            }

            is LoginAction.UserNameWritten -> {
                onActionUserWrite(action.user)
            }

            LoginAction.Error.BadCredentialsAccepted -> onActionBadCredentialsAccepted()

            LoginAction.Error.BackPressed -> onActionErrorBackPressed()
        }
    }

    private fun onActionBadCredentialsAccepted() {
        hideOverlap()
    }

    private fun onActionErrorBackPressed() {
        hideOverlap()
    }

    private fun showOverlap(overlap: LoginOverlap) {
        updateState {
            copy(overlap = overlap)
        }
    }

    private fun hideOverlap() {
        updateState {
            copy(overlap = null)
        }
    }

    private var pendingUser: User? = null

    override fun onBroadcastListenerSetup() {
        registerBackBroadcastListener(HomeViewModel::class.backBroadcastId) {
            pendingUser = it as User
        }
    }

    override fun onViewResumed() {
        super.onViewResumed()
        pendingUser?.also {
            postEvent(LoginEvent.LastUserAdded(it))
        }
        pendingUser = null
    }

    private fun doLogin() {
        sideEffect {
            updateState {
                copy(isLoading = true)
            }
            val userLogged =
                loginUseCase.invoke(LoginUseCase.Input(state.userName, state.userPassword))
            updateState {
                copy(isLoading = false)
            }
            userLogged.onSuccess { user ->
                postEvent(LoginEvent.Message(user.name))
                postEvent(LoginEvent.LoginSuccess(user))
            }.onFailure {
                showOverlap(LoginOverlap.ErrorBadCredentials)
            }
        }
    }

    private fun onActionLogin() {
        if (state.isLoading) {
            return
        }
        val userNameError = state.userName.isBlank()
        val passwordError = state.userPassword.isBlank()
        if (userNameError || passwordError) {
            updateState {
                copy(userNameError = userNameError, passwordError = passwordError)
            }
        } else {
            doLogin()
        }
    }

    private fun onActionDeleteUser() {
        updateState {
            copy(
                userName = STRING_EMPTY,
                userPassword = STRING_EMPTY,
                userNameError = false,
                passwordError = false
            )
        }
    }

    private fun onActionUserWrite(user: String) {
        // The text is kept in the state so it is restored if, for example, there is a device rotation
        // and the view is recreated
        updateState {
            copy(userName = user, userNameError = false)
        }
    }

    private fun onActionPasswordWrite(password: String) {
        updateState {
            copy(userPassword = password, passwordError = false)
        }
    }
}
