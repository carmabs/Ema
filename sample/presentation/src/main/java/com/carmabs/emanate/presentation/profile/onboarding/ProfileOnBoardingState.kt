package com.carmabs.emanate.presentation.profile.onboarding

import com.carmabs.domain.model.User
import com.carmabs.emanate.core.constants.STRING_EMPTY
import com.carmabs.emanate.core.state.EmaState
import com.carmabs.emanate.presentation.extension.fullName

data class ProfileOnBoardingState(val user: User?) : EmaState {

    companion object {
        val DEFAULT = ProfileOnBoardingState(
            user = null
        )
    }

    val userName
        get() = user?.fullName ?: STRING_EMPTY
}
