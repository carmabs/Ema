package com.carmabs.emanate.presentation.profile.onboarding

import com.carmabs.domain.model.Role
import com.carmabs.domain.model.User
import com.carmabs.emanate.core.source.SourceSetupScope
import com.carmabs.emanate.presentation.base.BaseViewModel

class ProfileOnBoardingViewModel(initialDataState: ProfileOnBoardingState) :
    BaseViewModel<ProfileOnBoardingState, ProfileOnBoardingActions, ProfileOnBoardingEvent>(initialDataState) {

    override fun SourceSetupScope<ProfileOnBoardingState, ProfileOnBoardingActions, ProfileOnBoardingEvent>.setup() {
        addReducer<ProfileOnBoardingInitializer> { initializer ->
            when (initializer) {
                is ProfileOnBoardingInitializer.Default -> state.copy(user = User(initializer.admin))
            }
        }

        addSideEffect<ProfileOnBoardingActions> { action ->
            val event = when (action) {
                ProfileOnBoardingActions.AdminClicked -> ProfileOnBoardingEvent.UserTypeSelected(Role.ADMIN)
                ProfileOnBoardingActions.UserClicked -> ProfileOnBoardingEvent.UserTypeSelected(Role.BASIC)
                ProfileOnBoardingActions.BackClicked -> ProfileOnBoardingEvent.OnBoardingCancelled
            }
            postEvent(event)
        }
    }
}
