package com.carmabs.emanate.core.source.sideeffect

/**
 * Created by Carlos Mateo Benito on 11/10/2026.
 *
 * What a side effect does when an action launches it while it is still running.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
enum class SideEffectConcurrency {
    /**
     * Every action launches it, in parallel with the running ones.
     */
    Every,

    /**
     * The running one is cancelled and the new one is launched. Useful to keep only the last request, for example
     * in a search.
     */
    Latest,

    /**
     * The new one is ignored while one is running. Useful to avoid repeating an operation, for example when a
     * button is tapped twice.
     */
    Leading
}
