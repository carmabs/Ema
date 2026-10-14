package com.carmabs.emanate.core.viewmodel

import com.carmabs.emanate.core.action.EmaAction

/**
 * Created by Carlos Mateo Benito on 14/10/2026.
 *
 * Lifecycle of the view, received by an EmaViewModel as actions, which its middlewares, reducers and side effects
 * handle like any other.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
sealed interface EmaLifecycle : EmaAction {

    /**
     * The view becomes visible.
     */
    data object Started : EmaLifecycle

    /**
     * The view is in the foreground.
     */
    data object Resumed : EmaLifecycle

    /**
     * The view is no longer fully visible.
     */
    data object Paused : EmaLifecycle

    /**
     * The view goes to the background.
     */
    data object Stopped : EmaLifecycle

    /**
     * The last action of the view model, given to its source as the last action when it is closed. The rest of its
     * work is cancelled after it, but its side effects can finish, up to their timeout or the closeTimeout of the
     * configuration, 5 seconds by default: they can broadcast actions and read the state, while the actions they
     * dispatch and the events they post are ignored, as the view model is closed.
     */
    data object Destroyed : EmaLifecycle

    override val type: String
        get() = "Lifecycle"
}
