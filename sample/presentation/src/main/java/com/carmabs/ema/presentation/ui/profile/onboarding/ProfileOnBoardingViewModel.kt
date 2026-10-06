package com.carmabs.ema.presentation.ui.profile.onboarding

import com.carmabs.domain.model.Role
import com.carmabs.domain.model.User
import com.carmabs.ema.core.initializer.EmaInitializer
import com.carmabs.ema.presentation.base.BaseViewModel

class ProfileOnBoardingViewModel(initialDataState: ProfileOnBoardingState) : BaseViewModel<ProfileOnBoardingState, ProfileOnBoardingActions, ProfileOnBoardingEvent>(
        initialDataState
    ){

    override fun onStateCreated(initializer: EmaInitializer?) {
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
}
