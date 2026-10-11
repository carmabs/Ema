package com.carmabs.emanate.presentation.home

import com.carmabs.domain.model.User
import com.carmabs.domain.usecase.GetUserFriendsUseCase
import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.broadcast.backBroadcastId
import com.carmabs.emanate.presentation.base.BaseViewModel
import com.carmabs.emanate.presentation.profile.creation.ProfileCreationViewModel

class HomeViewModel(private val getUserFriendsUseCase: GetUserFriendsUseCase, initialDataState: HomeState) :
    BaseViewModel<HomeState, HomeAction, HomeEvent>(initialDataState) {

    private lateinit var user: User
    override fun onStateCreated(initializer: EmaAction.Initializer?) {
        sideEffect {
            when (val homeInitializer = initializer as HomeInitializer) {
                is HomeInitializer.HomeUser -> {
                    user = homeInitializer.user
                    val friends = getUserFriendsUseCase(GetUserFriendsUseCase.Input(homeInitializer.user))
                    updateState {
                        copy(
                            userData = HomeState.UserData(
                                name = user.name,
                                surname = user.surname,
                                role = user.role
                            ),
                            userList = userList.toMutableList().apply {
                                addAll(friends)
                            }
                        )
                    }
                }
            }
        }
    }
    override fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.ProfileClicked -> {
                onActionCreateProfileClicked()
            }
        }
    }

    private fun onActionCreateProfileClicked() {
        postEvent(HomeEvent.ProfileClicked(user))
    }

    override fun onBroadcastListenerSetup() {
        registerBroadcastListener(ProfileCreationViewModel::class.backBroadcastId) {
            val user = it as User
            dispatchBroadcast(user)
            updateState {
                copy(
                    userList = userList.toMutableList().apply {
                        add(user)
                    }
                )
            }
        }
    }
}
