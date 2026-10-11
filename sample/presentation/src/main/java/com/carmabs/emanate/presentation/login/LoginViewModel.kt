package com.carmabs.emanate.presentation.login

import com.carmabs.domain.usecase.LoginUseCase
import com.carmabs.emanate.core.constants.STRING_EMPTY
import com.carmabs.emanate.core.model.onFailure
import com.carmabs.emanate.core.model.onSuccess
import com.carmabs.emanate.core.source.SourceSetupScope
import com.carmabs.emanate.core.source.sideeffect.SideEffectConcurrency
import com.carmabs.emanate.presentation.base.BaseViewModel
import com.carmabs.emanate.presentation.home.HomeBroadcast

class LoginViewModel(private val loginUseCase: LoginUseCase, initialDataState: LoginState) :
    BaseViewModel<LoginState, LoginAction, LoginEvent>(initialDataState) {

    override fun SourceSetupScope<LoginState, LoginAction, LoginEvent>.setup() {
        // The users added in the home screen
        addBroadcastListener<HomeBroadcast.UserAdded>()

        addReducer<LoginAction> { action ->
            when (action) {
                is LoginAction.UserNameWritten -> state.copy(userName = action.user, userNameError = false)

                is LoginAction.PasswordWritten -> state.copy(userPassword = action.password, passwordError = false)

                LoginAction.DeleteUser -> state.copy(
                    userName = STRING_EMPTY,
                    userPassword = STRING_EMPTY,
                    userNameError = false,
                    passwordError = false
                )

                LoginAction.Login -> if (state.isLoading) {
                    state
                } else {
                    state.copy(userNameError = state.userName.isBlank(), passwordError = state.userPassword.isBlank())
                }

                LoginRequest.Started -> state.copy(isLoading = true)

                is LoginRequest.Succeeded -> state.copy(isLoading = false)

                LoginRequest.Failed -> state.copy(isLoading = false, overlap = LoginOverlap.ErrorBadCredentials)

                LoginAction.Error.BadCredentialsAccepted,
                LoginAction.Error.BackPressed -> state.copy(overlap = null)
            }
        }

        // Leading: a second tap while the request is running is ignored
        addSideEffect<LoginAction.Login>(concurrency = SideEffectConcurrency.Leading) {
            if (state.isLoading || state.userNameError || state.passwordError) return@addSideEffect
            dispatch(LoginRequest.Started)
            loginUseCase(LoginUseCase.Input(state.userName, state.userPassword))
                .onSuccess { user -> dispatch(LoginRequest.Succeeded(user)) }
                .onFailure { dispatch(LoginRequest.Failed) }
        }

        addSideEffect<LoginRequest.Succeeded> { action ->
            postEvent(LoginEvent.Message(action.user.name))
            postEvent(LoginEvent.LoginSuccess(action.user))
        }

        addSideEffect<HomeBroadcast.UserAdded> { action ->
            postEvent(LoginEvent.LastUserAdded(action.user))
        }
    }
}
