package com.carmabs.emanate.core.action

/*
 * Created by Carlos Mateo Benito on 1/10/23.
 *
 * <p>
 * Copyright (c) 2023 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */

/**
 * Something that reaches the ViewModel: the actions of the user and the data a screen starts with.
 */
interface EmaAction {

    companion object {
        val type: String = "EmaAction"
    }

    val type: String
        get() = EmaAction.type

    /**
     * No action.
     */
    data object EMPTY : EmaAction

    sealed interface Lifecycle : EmaAction {
        data object Started : Lifecycle
        data object Resumed : Lifecycle
        data object Paused : Lifecycle
        data object Stopped : Lifecycle

        /**
         * The last action of the view model. The rest of its work is cancelled after it, but its side effects can
         * finish, up to their timeout or the destroyedTimeout of the configuration, 5 seconds by default:
         * they can broadcast actions and read the state, while the actions they dispatch and the events they post are
         * ignored, as the view model is closed.
         */
        data object Destroyed : Lifecycle

        override val type: String
            get() = "Lifecycle"
    }
}
