package com.carmabs.emanate.presentation.profile.onboarding

import com.carmabs.emanate.core.action.EmaAction
import kotlinx.serialization.Serializable

@Serializable
sealed class ProfileOnBoardingInitializer : EmaAction.Initializer {

    @Serializable
    data class Default(val admin: String) : ProfileOnBoardingInitializer()
}
