package com.carmabs.emanate.presentation.home

import com.carmabs.domain.model.User
import com.carmabs.emanate.core.state.EmaEvent

sealed interface HomeEvent : EmaEvent {
    data class ProfileClicked(val user: User) : HomeEvent
}
