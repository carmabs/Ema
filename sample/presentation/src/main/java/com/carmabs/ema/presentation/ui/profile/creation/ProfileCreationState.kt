package com.carmabs.ema.presentation.ui.profile.creation

import com.carmabs.domain.model.Role
import com.carmabs.ema.core.constants.STRING_EMPTY
import com.carmabs.ema.core.state.EmaState

data class ProfileCreationState(
    val role: Role,
    val name: String,
    val surname: String,
    val overlap: ProfileCreationOverlap? = null
) : EmaState {

    companion object {
        val DEFAULT = ProfileCreationState(
            Role.BASIC,
            STRING_EMPTY,
            STRING_EMPTY
        )
    }

    val roleText
        get() = role.name
}

