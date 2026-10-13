package com.carmabs.emanate.presentation.profile.creation

import com.carmabs.emanate.core.action.EmaAction
import kotlinx.serialization.Serializable

@Serializable
sealed class ProfileCreationInitializer : EmaAction.Initializer {
    @Serializable
    data object UserBasic : ProfileCreationInitializer()

    @Serializable
    data object Admin : ProfileCreationInitializer()
}
