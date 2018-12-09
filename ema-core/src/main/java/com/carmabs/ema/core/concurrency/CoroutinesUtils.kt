package com.carmabs.ema.core.concurrency

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope


/**
 * Utils for try catch staff for coroutines execution
 *
 * Whenever you call cancel(), a CancellationException is thrown and your asynchronous code
 * interrupts immediately because of the exception. The exception is thrown automatically for
 * any suspending function defined in the Kotlin coroutines library (kotlinx.coroutines) because all
 * those functions are cancellable. If you want to make your own coroutine cancellable, then you
 * need to handle the isActive property that is available in every coroutine context. That property
 * tells you if the coroutine is still active or has been canceled. This means that, in case you
 * have a long computation within your coroutine, you’ll have to periodically check the value of
 * isActive and throw a CancellationException whenever the coroutine is not active anymore
 * (of course, you can also choose to handle the cancellation of the coroutine differently if you
 * prefer and avoid throwing the exception if you don’t want it to be propagated).
 *
 * if we catch that exception and don’t let it flow through the coroutines code, there’s no way for
 * the coroutines or asynchronous tasks down in the stack to cancel.
 *
 * @author <a href=“mailto:carlos.mateo@babel.es”>Carlos Mateo</a>
 */

/**
 * Filters CancellationException for us and makes
 * sure it is propagated unless we explicitly state that we want to handle it manually.
 * @param tryBlock Function to try t execute
 * @param catchBlock Function to handle errors
 * @param handleCancellationExceptionManually Function to handle Cancellation Exception in coroutine
 */
suspend fun CoroutineScope.tryCatch(
        tryBlock: suspend CoroutineScope.() -> Unit,
        catchBlock: suspend CoroutineScope.(Throwable) -> Unit,
        handleCancellationExceptionManually: Boolean = false) {
    try {
        tryBlock()
    } catch (e: Throwable) {
        if (e !is CancellationException || handleCancellationExceptionManually) {
            catchBlock(e)
        } else {
            throw e
        }
    }
}

/**
 * Filters CancellationException for us and makes
 * sure it is propagated unless we explicitly state that we want to handle it manually.
 * @param tryBlock Function to try t execute
 * @param catchBlock Function to handle errors
 * @param finallyBlock Function to make in finally execution
 * @param handleCancellationExceptionManually Function to handle Cancellation Exception in coroutine
 */
suspend fun CoroutineScope.tryCatchFinally(
        tryBlock: suspend CoroutineScope.() -> Unit,
        catchBlock: suspend CoroutineScope.(Throwable) -> Unit,
        finallyBlock: suspend CoroutineScope.() -> Unit,
        handleCancellationExceptionManually: Boolean = false) {

    var caughtThrowable: Throwable? = null

    try {
        tryBlock()
    } catch (e: Throwable) {
        if (e !is CancellationException || handleCancellationExceptionManually) {
            catchBlock(e)
        } else {
            caughtThrowable = e
        }
    } finally {
        if (caughtThrowable is CancellationException && !handleCancellationExceptionManually) {
            throw caughtThrowable
        } else {
            finallyBlock()
        }
    }
}

/**
 * Filters CancellationException for us and makes
 * sure it is propagated unless we explicitly state that we want to handle it manually.
 * @param tryBlock Function to try t execute
 * @param finallyBlock Function to make in finally execution
 * @param handleCancellationExceptionManually Function to handle Cancellation Exception in coroutine
 */
suspend fun CoroutineScope.tryFinally(
        tryBlock: suspend CoroutineScope.() -> Unit,
        finallyBlock: suspend CoroutineScope.() -> Unit,
        suppressCancellationException: Boolean = false) {

    var caughtThrowable: Throwable? = null

    try {
        tryBlock()
    } catch (e: CancellationException) {
        if (!suppressCancellationException) {
            caughtThrowable = e
        }
    } finally {
        if (caughtThrowable is CancellationException && !suppressCancellationException) {
            throw caughtThrowable
        } else {
            finallyBlock()
        }
    }
}
