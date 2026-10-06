package com.carmabs.ema.presentation.ui.profile.onboarding

import com.carmabs.domain.model.User
import com.carmabs.ema.core.constants.STRING_EMPTY
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.presentation.extension.fullName

data class ProfileOnBoardingState(
    val user: User?
) : EmaState {

    companion object {
        val DEFAULT = ProfileOnBoardingState(
            user = null
        )
    }

    val userName
        get() = user?.fullName ?: STRING_EMPTY
}

