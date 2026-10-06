package com.carmabs.ema.presentation.ui.home

import com.carmabs.domain.model.User
import com.carmabs.ema.core.state.EmaEvent

sealed interface HomeEvent : EmaEvent {
    data class ProfileClicked(val user: User): HomeEvent
}
