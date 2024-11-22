package com.carmabs.ema.core.extension

import com.carmabs.ema.core.constants.LONG_ZERO
import kotlinx.coroutines.delay
import kotlin.time.Duration

/**
 * Execute the action with a minimum delay of the min time provided.If the action lasts less than min
 * time, the remaining time is applied. It it lasts more than min time, no delay is applied.
 */
suspend fun <T> minDelay(minTime: Duration, action: suspend () -> T): T {
    val initTime = System.currentTimeMillis()
    val resultValue = action.invoke()
    val timeSpent = System.currentTimeMillis() - initTime
    val differenceTime = minTime.inWholeMilliseconds - timeSpent
    if (differenceTime > LONG_ZERO) {
        delay(differenceTime)
    }
    return resultValue
}