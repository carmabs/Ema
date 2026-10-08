package com.carmabs.ema.core.action

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

    /**
     * The data a screen starts with. The ViewModel receives it once, in onStateCreated, the first time the screen
     * is created.
     */
    interface Initializer : EmaAction {
        companion object {
            /**
             * Key of the initializer in bundles and routes.
             */
            const val KEY = "EmaInitializer"
        }

        override val type: String
            get() = "Initializer"

        /**
         * No initializer.
         */
        data object EMPTY : Initializer
    }

    sealed interface Lifecycle : EmaAction {
        data object Started : Lifecycle
        data object Resumed : Lifecycle
        data object Paused : Lifecycle
        data object Stopped : Lifecycle

        data object Destroyed : Lifecycle

        override val type: String
            get() = "Lifecycle"
    }
}
