package com.carmabs.emanate.presentation.profile.creation

import com.carmabs.domain.model.Role
import com.carmabs.domain.model.User
import com.carmabs.emanate.presentation.base.BaseViewModel

class ProfileCreationViewModel(initialDataState: ProfileCreationState) :
    BaseViewModel<ProfileCreationState, ProfileCreationAction, ProfileCreationEvent>(initialDataState) {

    override fun setup() = source {
        reducers {
            add<ProfileCreationInitializer> { initializer ->
                when (initializer) {
                    ProfileCreationInitializer.Admin -> state.copy(role = Role.ADMIN)
                    ProfileCreationInitializer.UserBasic -> state.copy(role = Role.BASIC)
                }
            }

            add<ProfileCreationAction> { action ->
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
        }

        sideEffects {
            // The home screen adds the user to its list
            add<ProfileCreationAction.DialogConfirmClicked> {
                val user = User(state.name, state.surname, state.role)
                broadcast(ProfileCreationBroadcast.UserCreated(user), keepUntilReceived = true)
                postEvent(ProfileCreationEvent.DialogConfirmationAccepted)
            }

            add<ProfileCreationAction.DialogBackConfirm> {
                postEvent(ProfileCreationEvent.DialogConfirmationAccepted)
            }
        }
    }
}
