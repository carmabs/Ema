package com.carmabs.emanate.core.logging

/**
 * Created by Carlos Mateo Benito on 11/10/2026.
 *
 * Writes the logs of Emanate, for example with Log.d in Android.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
fun interface EmaLogger {

    companion object {
        /**
         * Writes with println, adding EMA_ before the tag. It is the logger of the default configuration.
         */
        val Println: EmaLogger = EmaLogger(prefix = "EMA_") { tag, message -> println("$tag: $message") }
    }

    /**
     * Writes [message] with [tag].
     */
    fun log(tag: String, message: String)
}

/**
 * Creates an [EmaLogger] that adds [prefix] before the tag of every log, if it isn't null. Useful to filter all the
 * logs of Emanate.
 */
@Suppress("FunctionName") // Factory named like the interface
fun EmaLogger(prefix: String?, log: (tag: String, message: String) -> Unit): EmaLogger =
    EmaLogger { tag, message -> log(prefix?.let { it + tag } ?: tag, message) }
