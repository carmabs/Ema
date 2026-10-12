package com.carmabs.emanate.core.extension

import kotlin.time.Duration
import kotlin.time.TimeSource
import kotlinx.coroutines.delay

/**
 * Runs [action] and makes it last at least [duration]. If it finishes before, it waits the remaining time; if it lasts
 * longer, it doesn't wait. Useful to keep a loading visible long enough to be seen.
 * @return the result of [action]
 */
suspend fun <T> requireMinDurationOf(duration: Duration, action: suspend () -> T): T {
    val initTime = TimeSource.Monotonic.markNow()
    val resultValue = action.invoke()
    // delay doesn't wait if the remaining time is zero or negative
    delay(duration - initTime.elapsedNow())
    return resultValue
}
