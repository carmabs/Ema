package com.carmabs.ema.presentation.ui.profile.onboarding

import com.carmabs.domain.model.Role
import com.carmabs.ema.core.state.EmaEvent

sealed interface ProfileOnBoardingEvent : EmaEvent {
    data class UserTypeSelected(val role: Role) : ProfileOnBoardingEvent
}
