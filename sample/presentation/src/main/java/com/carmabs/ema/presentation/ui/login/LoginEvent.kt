package com.carmabs.ema.presentation.ui.login

import com.carmabs.domain.model.User
import com.carmabs.ema.core.state.EmaEvent

sealed interface LoginEvent : EmaEvent {

    data class Message(val userName: String) : LoginEvent

    data class LastUserAdded(val user: User) : LoginEvent

    data class LoginSuccess(val user: User) : LoginEvent
}
