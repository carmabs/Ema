package com.carmabs.emanate.android.logging

import android.util.Log
import com.carmabs.emanate.core.logging.EmaLogger

/**
 * Writes the logs to logcat with Log.d, adding EMA_ before the tag. It is the logger of EmaConfiguration.Android in
 * debug builds.
 */
val EmaLogger.Companion.Logcat: EmaLogger
    get() = EmaLogger(prefix = "EMA_") { tag, message -> Log.d(tag, message) }
