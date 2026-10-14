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
     * The last action of the view model, right before its source is closed. Its side effects are cancelled when the
     * source is closed, like the rest of its work, unless they survive close: work that must run when the view model
     * is closed, like sending a result to the previous screen, goes in the onClose block of the side effects.
     */
    data object Destroyed : EmaLifecycle

    override val type: String
        get() = "Lifecycle"
}
