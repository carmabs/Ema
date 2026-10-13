package com.carmabs.emanate.presentation.profile.onboarding

import com.carmabs.domain.model.Role
import com.carmabs.domain.model.User
import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.presentation.base.BaseActionViewModel

class ProfileOnBoardingActionViewModel(initialDataState: ProfileOnBoardingState) :
    BaseActionViewModel<ProfileOnBoardingState, ProfileOnBoardingActions, ProfileOnBoardingEvent>(
        initialDataState
    ) {

    override fun onStateCreated(initializer: EmaAction.Initializer?) {
        when (val onBoardingInitializer = initializer as ProfileOnBoardingInitializer) {
            is ProfileOnBoardingInitializer.Default -> {
                updateState {
                    copy(user = User(onBoardingInitializer.admin))
                }
            }
        }
    }

    override fun onAction(action: ProfileOnBoardingActions) {
        when (action) {
            ProfileOnBoardingActions.AdminClicked -> onActionAdminClicked()
            ProfileOnBoardingActions.UserClicked -> onActionUserClicked()
            ProfileOnBoardingActions.BackClicked -> onActionBackClicked()
        }
    }
    private fun onActionAdminClicked() {
        postEvent(
            ProfileOnBoardingEvent.UserTypeSelected(Role.ADMIN)
        )
    }

    private fun onActionUserClicked() {
        postEvent(
            ProfileOnBoardingEvent.UserTypeSelected(Role.BASIC)
        )
    }

    private fun onActionBackClicked() {
        postEvent(ProfileOnBoardingEvent.OnBoardingCancelled)
    }
}
