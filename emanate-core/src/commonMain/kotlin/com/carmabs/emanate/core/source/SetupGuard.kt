package com.carmabs.emanate.core.source

/**
 * Created by Carlos Mateo Benito on 12/10/2026.
 *
 * Shared by the scopes of setup, so nothing can be added to the source after setup finishes, even with a scope kept
 * outside of it.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
internal class SetupGuard {

    private var built = false

    /**
     * Called when setup finishes. Nothing can be added after it.
     */
    fun build() {
        built = true
    }

    fun checkNotBuilt() {
        check(!built) { "The source can only be configured inside setup" }
    }
}
