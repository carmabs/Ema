package com.carmabs.ema.presentation.profile.creation

import com.carmabs.ema.core.action.EmaAction
import kotlinx.serialization.Serializable

@Serializable
sealed class ProfileCreationInitializer : EmaAction.Initializer {
    @Serializable
    data object UserBasic : ProfileCreationInitializer()

    @Serializable
    data object Admin : ProfileCreationInitializer()
}
