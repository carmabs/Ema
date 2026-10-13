package com.carmabs.emanate.core.utils

import kotlin.time.Duration
import kotlin.time.TimeSource
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout

/**
 * Coroutine utilities. Use them through `EmaUtils.Coroutines`.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
object EmaCoroutineUtils {

    /**
     * Suspends until [block] resumes the continuation, like suspendCancellableCoroutine, but fails with a
     * TimeoutCancellationException if it is not resumed within [duration]. Useful to wrap callback based APIs that may
     * never answer.
     */
    suspend inline fun <T> suspendCoroutineWithTimeout(
        duration: Duration,
        crossinline block: (CancellableContinuation<T>) -> Unit
    ): T = withTimeout(duration) {
        suspendCancellableCoroutine(block = block)
    }

    /**
     * Runs [action] and makes it last at least [duration]. If it finishes before, it waits the remaining time; if it
     * lasts longer, it doesn't wait. Useful to keep a loading visible long enough to be seen.
     * @return the result of [action]
     */
    suspend fun <T> requireMinDurationOf(duration: Duration, action: suspend () -> T): T {
        val initTime = TimeSource.Monotonic.markNow()
        val resultValue = action.invoke()
        // delay doesn't wait if the remaining time is zero or negative
        delay(duration - initTime.elapsedNow())
        return resultValue
    }
}
