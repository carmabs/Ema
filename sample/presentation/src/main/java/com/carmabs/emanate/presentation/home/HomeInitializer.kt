package com.carmabs.emanate.presentation.home

import com.carmabs.domain.model.User
import com.carmabs.emanate.core.initializer.EmaInitializer
import kotlinx.serialization.Serializable

@Serializable
sealed interface HomeInitializer : EmaInitializer {
    @Serializable
    data class HomeUser(val user: User) : HomeInitializer
}
