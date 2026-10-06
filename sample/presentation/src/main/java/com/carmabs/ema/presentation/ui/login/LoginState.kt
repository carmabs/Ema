package com.carmabs.ema.presentation.ui.login

import com.carmabs.ema.core.constants.STRING_EMPTY
import com.carmabs.ema.core.state.EmaState

data class LoginState(
    val userName: String,
    val userPassword: String,
    val isLoading: Boolean,
    val userNameError: Boolean,
    val passwordError: Boolean,
    val overlap: LoginOverlap?
) : EmaState {

    companion object {
        val DEFAULT = LoginState(
            userName = STRING_EMPTY,
            userPassword = STRING_EMPTY,
            isLoading = false,
            userNameError = false,
            passwordError = false,
            overlap = null
        )
    }
}
