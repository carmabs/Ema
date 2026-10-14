package com.carmabs.emanate.core.source

import com.carmabs.emanate.core.state.EmaState

/**
 * Created by Carlos Mateo Benito on 15/10/2026.
 *
 * Feature that the source doesn't know, added to it with install in setup, like an
 * [com.carmabs.emanate.core.savestate.EmaBackup]. It adds its pieces to the setup, like any other: reducers,
 * middlewares, side effects or broadcast listeners, so the source keeps handling only actions.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
interface EmaSourcePlugin<S : EmaState> {

    /**
     * Adds the pieces of the plugin to [setup], in the place where it is installed.
     */
    fun install(setup: SourceSetupScope<S, *, *>)
}
