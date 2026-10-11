package com.carmabs.emanate.presentation.home

import com.carmabs.domain.model.User
import com.carmabs.emanate.core.action.EmaAction

/**
 * Created by Carlos Mateo Benito on 6/3/24.
 *
 * <p>
 * Copyright (c) 2024 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
sealed interface HomeAction : EmaAction {
    data object ProfileClicked : HomeAction
}

/**
 * The friends of the user have been loaded. Only dispatched by the side effects of HomeViewModel.
 */
internal data class HomeFriendsLoaded(val friends: List<User>) : HomeAction
