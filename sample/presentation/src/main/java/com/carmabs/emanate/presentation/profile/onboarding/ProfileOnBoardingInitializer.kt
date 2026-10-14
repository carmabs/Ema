package com.carmabs.emanate.presentation.profile.onboarding

import com.carmabs.emanate.core.initializer.EmaInitializer
import kotlinx.serialization.Serializable

@Serializable
sealed class ProfileOnBoardingInitializer : EmaInitializer {

    @Serializable
    data class Default(val admin: String) : ProfileOnBoardingInitializer()
}
