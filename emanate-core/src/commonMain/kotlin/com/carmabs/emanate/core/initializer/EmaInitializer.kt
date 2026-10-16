package com.carmabs.emanate.core.initializer

import com.carmabs.emanate.core.action.EmaAction

/**
 * Created by Carlos Mateo Benito on 14/10/2026.
 *
 * The data a screen starts with. The view model receives it once, the first time the screen is created: an
 * EmaViewModel as an action, before the saved state, and a classic view model in onStateCreated.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
interface EmaInitializer : EmaAction {
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
    data object EMPTY : EmaInitializer
}
