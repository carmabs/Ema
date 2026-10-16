package com.carmabs.emanate.core.savestate

/**
 * Created by Carlos Mateo Benito on 13/10/2026.
 *
 * Keeps the values of an [EmaBackup] while its process is alive and after the system kills it, so they can be
 * restored when the screen is created again. Each platform gives its own store: on Android, emanate-android gives the
 * view model one over the SavedStateHandle of the screen when it creates it. Pass [None] where nothing should be saved.
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

    /**
     * Saves nothing, for the platforms where the state doesn't have to survive the death of the process.
     */
    data object None : EmaStateStore {

        override fun read(key: String): String? = null

        override fun write(key: String, value: String?) = Unit
    }
}
