package com.carmabs.ema.presentation.home

import com.carmabs.domain.model.User
import com.carmabs.ema.core.action.EmaAction
import kotlinx.serialization.Serializable

@Serializable
sealed interface HomeInitializer : EmaAction.Initializer {
    @Serializable
    data class HomeUser(val user: User) : HomeInitializer
}
