package com.carmabs.ema.core.action

/**
 * Created by Carlos Mateo Benito on 1/10/23.
 *
 * <p>
 * Copyright (c) 2023 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
interface EmaAction {

    companion object {
        val type: String = "EmaAction"
        data object EMPTY : EmaAction
    }

    val type: String
        get() = EmaAction.type

    interface Initializer : EmaAction {
        override val type: String
            get() = "Initializer"
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

    interface Screen : EmaAction {
        override val type: String
            get() = "Screen"

        object EMPTY : Screen
    }
}
