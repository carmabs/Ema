package com.carmabs.ema.core.concurrency

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers

/**
 * Manager to handle asynchronous task
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */


interface AsyncManager {
    /**
     * Method to make async task. Not blocking. To get result use blocking  Deferred.await()
     * @param T Return object when call is finished
     * @param block Function to execute in asynchronous task
     * @param dispatcher @param dispatcher Executor thread
     */
    suspend fun <T> async(dispatcher: CoroutineDispatcher = Dispatchers.Main,block: suspend CoroutineScope.() -> T): Deferred<T>

    /**
     * Blocking method to make async task.
     * @param T Return object when call is finished
     * @param block Function to execute in asynchronous task
     * @param dispatcher @param dispatcher Executor thread
     */
    suspend fun <T> asyncAwait(dispatcher: CoroutineDispatcher = Dispatchers.Main,block: suspend CoroutineScope.() -> T): T

    /**
     * Cancel all async tasks in process
     */
    fun cancelAllAsync()

    /**
     * Cancel an specific task through its deferred object
     * @param deferred Task to cancel
     */
    fun cancelAsync(deferred: Deferred<*>)
}