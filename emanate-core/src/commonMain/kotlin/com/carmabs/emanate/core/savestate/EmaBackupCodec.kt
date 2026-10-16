package com.carmabs.emanate.core.savestate

/**
 * Created by Carlos Mateo Benito on 14/10/2026.
 *
 * Converts a saved value of type [T] to the text kept by the [EmaStateStore], and back. [Text] saves text as it is;
 * emanate-serialization gives one that encodes any serializable type in JSON, and you can write your own, for example
 * to save an enum by its name.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
interface EmaBackupCodec<T> {

    /**
     * Returns the text saved for [value].
     */
    fun encode(value: T): String

    /**
     * Returns the value of the saved [value], or null if it isn't valid, for example because it was saved by a
     * previous version of the app. A null value isn't restored.
     */
    fun decode(value: String): T?

    /**
     * Saves text as it is.
     */
    data object Text : EmaBackupCodec<String> {

        override fun encode(value: String): String = value

        override fun decode(value: String): String = value
    }
}
