package com.carmabs.ema.core.extension

import com.carmabs.ema.core.constants.LONG_ZERO
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.TimeSource

/**
 * Execute the action with a minimum delay of the min time provided.If the action lasts less than min
 * time, the remaining time is applied. It it lasts more than min time, no delay is applied.
 */
suspend fun <T> minDelay(minTime: Duration, action: suspend () -> T): T {
    val initTime = TimeSource.Monotonic.markNow()
    val resultValue = action.invoke()
    val differenceTime = minTime.inWholeMilliseconds - initTime.elapsedNow().inWholeMilliseconds
    if (differenceTime > LONG_ZERO) {
        delay(differenceTime)
    }
    return resultValue
}