package com.carmabs.emanate.presentation.home

import com.carmabs.domain.model.User
import com.carmabs.domain.usecase.GetUserFriendsUseCase
import com.carmabs.emanate.core.source.SourceSetupScope
import com.carmabs.emanate.presentation.base.BaseViewModel
import com.carmabs.emanate.presentation.profile.creation.ProfileCreationBroadcast

class HomeViewModel(private val getUserFriendsUseCase: GetUserFriendsUseCase, initialDataState: HomeState) :
    BaseViewModel<HomeState, HomeAction, HomeEvent>(initialDataState) {

    override fun SourceSetupScope<HomeState, HomeAction, HomeEvent>.setup() {
        // The users created in the profile screen
        addBroadcastListener<ProfileCreationBroadcast.UserCreated>()

        addReducer<HomeInitializer> { initializer ->
            when (initializer) {
                is HomeInitializer.HomeUser -> state.copy(
                    userData = HomeState.UserData(
                        name = initializer.user.name,
                        surname = initializer.user.surname,
                        role = initializer.user.role
                    )
                )
            }
        }
        addReducer<HomeFriendsLoaded> { action -> state.copy(userList = state.userList + action.friends) }
        addReducer<ProfileCreationBroadcast.UserCreated> { action ->
            state.copy(userList = state.userList + action.user)
        }

        addSideEffect<HomeInitializer> { initializer ->
            when (initializer) {
                is HomeInitializer.HomeUser -> {
                    val friends = getUserFriendsUseCase(GetUserFriendsUseCase.Input(initializer.user))
                    dispatch(HomeFriendsLoaded(friends))
                }
            }
        }
        addSideEffect<HomeAction.ProfileClicked> {
            state.userData?.also { postEvent(HomeEvent.ProfileClicked(User(it.name, it.surname, it.role))) }
        }
        // The login screen shows the last user added
        addSideEffect<ProfileCreationBroadcast.UserCreated> { action ->
            broadcast(HomeBroadcast.UserAdded(action.user), keepUntilReceived = true)
        }
    }
}
