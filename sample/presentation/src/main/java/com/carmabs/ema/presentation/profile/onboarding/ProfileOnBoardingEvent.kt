package com.carmabs.ema.presentation.profile.onboarding

import com.carmabs.domain.model.Role
import com.carmabs.ema.core.state.EmaEvent

sealed interface ProfileOnBoardingEvent : EmaEvent {
    data class UserTypeSelected(val role: Role) : ProfileOnBoardingEvent
    data object OnBoardingCancelled : ProfileOnBoardingEvent
}
