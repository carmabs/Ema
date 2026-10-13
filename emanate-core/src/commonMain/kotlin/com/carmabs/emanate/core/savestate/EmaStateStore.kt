package com.carmabs.emanate.core.savestate

/**
 * Created by Carlos Mateo Benito on 13/10/2026.
 *
 * Keeps the values saved by a view model while its process is alive and after the system kills it, so they can be
 * restored when the screen is created again. The view models only save what they declare with saved in setup. Each
 * platform gives its own store: on Android, emanate-android gives one over the SavedStateHandle of the screen. If a
 * platform gives none, nothing is saved.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
interface EmaStateStore {

    /**
     * Returns the value saved with [key], or null if there isn't any.
     */
    fun read(key: String): String?

    /**
     * Saves [value] with [key], replacing the previous one. Null removes it.
     */
    fun write(key: String, value: String?)
}
