package com.carmabs.ema.presentation.ui.login

import com.carmabs.ema.core.constants.STRING_EMPTY
import com.carmabs.ema.core.state.EmaState

data class LoginState(
    val userName: String,
    val userPassword: String,
    val overlap: LoginOverlap?
) : EmaState {

    companion object {
        val DEFAULT = LoginState(
            userName = STRING_EMPTY,
            userPassword = STRING_EMPTY,
            overlap = null
        )
    }
}

