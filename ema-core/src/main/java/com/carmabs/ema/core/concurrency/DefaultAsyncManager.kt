package com.carmabs.ema.core.concurrency

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

/**
 * AsyncManager implementation for coroutines
 *
 * @author <a href=“mailto:carlos.mateo@babel.es”>Carlos Mateo</a>
 */

class DefaultAsyncManager : AsyncManager {


    private val deferredList: MutableList<Deferred<*>> = mutableListOf()

    /**
     * Method to make async task. Not blocking. To get result use blocking  Deferred.await()
     * Add each deferred object to a list to make available its cancellation
     * @param T Return object when call is finished
     * @param block Function to execute in asynchronous task
     */
    override suspend fun <T> async(block: suspend CoroutineScope.() -> T): Deferred<T> {
        val deferred: Deferred<T> = GlobalScope.async { block() }
        deferredList.add(deferred)
        deferred.invokeOnCompletion { deferredList.remove(deferred) }
        return deferred
    }

    /**
     * Blocking method to make async task.
     * @param T Return object when call is finished
     * @param block Function to execute in asynchronous task
     */
    override suspend fun <T> asyncAwait(block: suspend CoroutineScope.() -> T): T {
        return async(block).await()
    }

    /**
     * Cancel all async tasks saved in deferred list
     */
    override fun cancelAllAsync() {
        deferredList.forEach { it.cancel() }
        deferredList.clear()
    }

    /**
     * Cancel an specific task through its deferred object
     * @param deferred Task to cancel
     */
    override fun cancelAsync(deferred: Deferred<*>) {
        if(deferredList.remove(deferred))
            deferred.cancel()
    }
}