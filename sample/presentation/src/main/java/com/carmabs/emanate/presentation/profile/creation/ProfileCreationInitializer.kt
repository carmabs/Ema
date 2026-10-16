package com.carmabs.emanate.presentation.profile.creation

import com.carmabs.emanate.core.initializer.EmaInitializer
import kotlinx.serialization.Serializable

@Serializable
sealed class ProfileCreationInitializer : EmaInitializer {
    @Serializable
    data object UserBasic : ProfileCreationInitializer()

    @Serializable
    data object Admin : ProfileCreationInitializer()
}
