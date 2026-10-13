package com.carmabs.emanate.presentation.home

import com.carmabs.domain.model.User
import com.carmabs.emanate.core.action.EmaAction

/**
 * Actions broadcast by the home view models. Their type is the id of the broadcast.
 *
 * <p>
 * Copyright (c) 2026, Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
sealed interface HomeBroadcast : EmaAction {

    /**
     * A user has been added to the list of friends
     */
    data class UserAdded(val user: User) : HomeBroadcast
}
