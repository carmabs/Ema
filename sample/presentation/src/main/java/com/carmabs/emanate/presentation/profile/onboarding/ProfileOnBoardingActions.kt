package com.carmabs.emanate.presentation.profile.onboarding

import com.carmabs.emanate.core.action.EmaAction

sealed interface ProfileOnBoardingActions : EmaAction {
    data object AdminClicked : ProfileOnBoardingActions
    data object UserClicked : ProfileOnBoardingActions
    data object BackClicked : ProfileOnBoardingActions
}
