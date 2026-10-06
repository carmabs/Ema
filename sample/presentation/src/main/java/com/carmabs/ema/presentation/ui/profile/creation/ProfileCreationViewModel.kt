package com.carmabs.ema.presentation.ui.profile.creation

import com.carmabs.domain.model.Role
import com.carmabs.domain.model.User
import com.carmabs.ema.core.initializer.EmaInitializer
import com.carmabs.ema.presentation.base.BaseViewModel

class ProfileCreationViewModel(
    initialDataState: ProfileCreationState
) : BaseViewModel<ProfileCreationState, ProfileCreationAction, ProfileCreationEvent>(
    initialDataState
) {
    override fun onStateCreated(initializer: EmaInitializer?) {
        when (initializer as ProfileCreationInitializer) {
            ProfileCreationInitializer.Admin -> updateState {
                copy(role = Role.ADMIN)
            }

            ProfileCreationInitializer.UserBasic -> updateState {
                copy(role = Role.BASIC)
            }
        }
    }

    override fun onAction(action: ProfileCreationAction) {
        when (action) {
            ProfileCreationAction.CreateClicked -> onActionCreateClicked()
            ProfileCreationAction.DialogCancelClicked -> onActionDialogCancelClicked()
            ProfileCreationAction.DialogConfirmClicked -> onActionDialogConfirmClicked()
            is ProfileCreationAction.UserNameWritten -> onActionUserNameWritten(action.name)
            is ProfileCreationAction.UserSurnameWritten -> onActionUserSurnameWritten(action.surname)
            ProfileCreationAction.OnBack -> onActionBack()
            ProfileCreationAction.DialogBackCancel -> onActionBackCancel()
            ProfileCreationAction.DialogBackConfirm -> onActionBackConfirmed()
        }
    }

    private fun showOverlap(overlap: ProfileCreationOverlap) {
        updateState {
            copy(overlap = overlap)
        }
    }

    private fun hideOverlap() {
        updateState {
            copy(overlap = null)
        }
    }

    private fun onActionBackCancel() {
        hideOverlap()
    }

    private fun onActionBackConfirmed() {
        hideOverlap()
        postEvent(ProfileCreationEvent.DialogConfirmationAccepted)
    }

    private fun onActionUserNameWritten(name: String) {
        updateState {
            copy(name = name)
        }
    }

    private fun onActionUserSurnameWritten(surname: String) {
        updateState {
            copy(surname = surname)
        }
    }

    private fun onActionCreateClicked() {
        if (!state.canCreate)
            return
        showOverlap(ProfileCreationOverlap.DialogUserCreated(state.role))
    }

    private fun onActionDialogConfirmClicked() {
        hideOverlap()
        dispatchBroadcast(User(state.name, state.surname, state.role))
        postEvent(ProfileCreationEvent.DialogConfirmationAccepted)
    }

    private fun onActionDialogCancelClicked() {
        hideOverlap()
    }


    private fun onActionBack() {
        showOverlap(ProfileCreationOverlap.DialogBackConfirmation)
    }
}
