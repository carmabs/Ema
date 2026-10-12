package com.carmabs.emanate.presentation.profile.creation

import com.carmabs.domain.model.Role
import com.carmabs.domain.model.User
import com.carmabs.emanate.core.source.SourceSetupScope
import com.carmabs.emanate.presentation.base.BaseViewModel

class ProfileCreationViewModel(initialDataState: ProfileCreationState) :
    BaseViewModel<ProfileCreationState, ProfileCreationAction, ProfileCreationEvent>(initialDataState) {

    override fun SourceSetupScope<ProfileCreationState, ProfileCreationAction, ProfileCreationEvent>.setup() {
        addReducer<ProfileCreationInitializer> { initializer ->
            when (initializer) {
                ProfileCreationInitializer.Admin -> state.copy(role = Role.ADMIN)
                ProfileCreationInitializer.UserBasic -> state.copy(role = Role.BASIC)
            }
        }

        addReducer<ProfileCreationAction> { action ->
            when (action) {
                is ProfileCreationAction.UserNameWritten -> state.copy(name = action.name)

                is ProfileCreationAction.UserSurnameWritten -> state.copy(surname = action.surname)

                ProfileCreationAction.CreateClicked -> if (state.canCreate) {
                    state.copy(overlap = ProfileCreationOverlap.DialogUserCreated(state.role))
                } else {
                    state
                }

                ProfileCreationAction.OnBack -> state.copy(overlap = ProfileCreationOverlap.DialogBackConfirmation)

                ProfileCreationAction.DialogConfirmClicked,
                ProfileCreationAction.DialogCancelClicked,
                ProfileCreationAction.DialogBackConfirm,
                ProfileCreationAction.DialogBackCancel -> state.copy(overlap = null)
            }
        }

        // The home screen adds the user to its list
        addSideEffect<ProfileCreationAction.DialogConfirmClicked> {
            val user = User(state.name, state.surname, state.role)
            broadcast(ProfileCreationBroadcast.UserCreated(user), keepUntilReceived = true)
            postEvent(ProfileCreationEvent.DialogConfirmationAccepted)
        }

        addSideEffect<ProfileCreationAction.DialogBackConfirm> {
            postEvent(ProfileCreationEvent.DialogConfirmationAccepted)
        }
    }
}
