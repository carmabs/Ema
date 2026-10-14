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
 * Something that reaches a source: the actions of the user, the data a screen starts with or the lifecycle of the
 * view.
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
}
