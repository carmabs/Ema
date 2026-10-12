package com.carmabs.emanate.core.extension

import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout

/**
 * Suspends until [block] resumes the continuation, like suspendCancellableCoroutine, but fails with a
 * TimeoutCancellationException if it is not resumed within [duration]. Useful to wrap callback based APIs that may
 * never answer.
 */
suspend inline fun <T> suspendCoroutineWithTimeout(
    duration: Duration,
    crossinline block: (CancellableContinuation<T>) -> Unit
) = withTimeout(duration) {
    suspendCancellableCoroutine(block = block)
}

/**
 * Creates a [CoroutineScope] with this context. If the context has no Job, the scope creates one.
 */
fun CoroutineContext.toScope() = CoroutineScope(this)
