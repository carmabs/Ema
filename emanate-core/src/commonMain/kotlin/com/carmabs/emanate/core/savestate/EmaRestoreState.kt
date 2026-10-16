package com.carmabs.emanate.core.savestate

import com.carmabs.emanate.core.action.EmaAction

/**
 * Created by Carlos Mateo Benito on 14/10/2026.
 *
 * Restores the saved [values], by key, in a source. The reducer of each [EmaBackup] installed in the source restores
 * its values, so the source doesn't know them, and the reducers and middlewares of the source receive it like any
 * other action, for example to validate the restored state. Only the [EmaBackup] dispatches it, so its
 * constructor is internal: emanate-test creates it for the tests as a friend module.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
@ConsistentCopyVisibility
data class EmaRestoreState internal constructor(val values: Map<String, String>) : EmaAction
