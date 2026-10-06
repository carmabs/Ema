package com.carmabs.ema.presentation.ui.login

import com.carmabs.domain.model.User
import com.carmabs.domain.usecase.LoginUseCase
import com.carmabs.ema.core.broadcast.backBroadcastId
import com.carmabs.ema.core.constants.STRING_EMPTY
import com.carmabs.ema.core.initializer.EmaInitializer
import com.carmabs.ema.core.model.onFailure
import com.carmabs.ema.core.model.onSuccess
import com.carmabs.ema.presentation.base.BaseViewModel
import com.carmabs.ema.presentation.ui.home.HomeViewModel

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    initialDataState: LoginState
) : BaseViewModel<LoginState, LoginAction, LoginEvent>(initialDataState) {
    override fun onStateCreated(initializer: EmaInitializer?) = Unit
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
            LoginAction.Error.PasswordEmptyAccepted -> onActionErrorPasswordEmptyAccepted()
            LoginAction.Error.UserEmptyAccepted -> onActionErrorUserEmptyAccepted()
        }
    }

    private fun onActionErrorUserEmptyAccepted() {
        hideOverlap()
    }

    private fun onActionErrorPasswordEmptyAccepted() {
        hideOverlap()
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
            showOverlap(LoginOverlap.Loading)
            val userLogged =
                loginUseCase.invoke(LoginUseCase.Input(state.userName, state.userPassword))
            hideOverlap()
            userLogged.onSuccess {user->
                postEvent(LoginEvent.Message(user.name))
                postEvent(LoginEvent.LoginSuccess(user))
            }.onFailure {
                showOverlap(LoginOverlap.ErrorBadCredentials)
            }

        }
    }

    private fun onActionLogin() {
        when {
            state.userName.isEmpty() -> showOverlap(LoginOverlap.ErrorUserEmpty)

            state.userPassword.isEmpty() -> showOverlap(LoginOverlap.ErrorPasswordEmpty)

            else -> doLogin()
        }
    }

    private fun onActionDeleteUser() {
        updateState {
            copy(
                userName = STRING_EMPTY,
                userPassword = STRING_EMPTY
            )
        }
    }


    private fun onActionUserWrite(user: String) {
        //The view is notified, but bindForUpdate in the view only sets the text when it differs from the
        //previous state, so it doesn't conflict with the user typing. The state is kept if, for example, there is a device
        //rotation and the view is recreated, to set the text with last value saved on state
        updateState {
            copy(userName = user)
        }
    }

    private fun onActionPasswordWrite(password: String) {
        updateState {
            copy(userPassword = password)
        }
    }

}
