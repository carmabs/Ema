package com.carmabs.ema.presentation.home

import com.carmabs.domain.model.Role
import com.carmabs.domain.model.User
import com.carmabs.ema.core.state.EmaState

data class HomeState(val userData: UserData?, val userList: List<User>) : EmaState {

    companion object {
        val DEFAULT = HomeState(
            userData = null,
            userList = emptyList()
        )
    }

    data class UserData(val name: String, val surname: String, val role: Role)

    val showCreateButton
        get() = userData?.role == Role.ADMIN

    val showUserList
        get() = userData != null

    val showEmptyList
        get() = showUserList && userList.isEmpty()
}
