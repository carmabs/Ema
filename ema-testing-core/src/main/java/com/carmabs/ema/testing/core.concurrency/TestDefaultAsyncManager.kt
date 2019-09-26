package com.carmabs.ema.testing.core.concurrency

import com.carmabs.ema.core.concurrency.AsyncManager
import com.carmabs.ema.core.concurrency.DefaultAsyncManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers

/**
 * AsyncManager testing implementation for coroutines
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
class TestDefaultAsyncManager : AsyncManager {

    private val defaultAsyncManager = DefaultAsyncManager()

    /**
     * Method to make async task. Not blocking. To get result use blocking  Deferred.await()
     * Add each deferred object to a list to make available its cancellation
     * @param T Return object when call is finished
     * @param block Function to execute in asynchronous task
     * @param dispatcher Executor thread
     * @param fullException If its is true, an exception launched on some child task affects to the
     * rest of task, including the parent one, if it is false, only affect to the child class
     */
    override suspend fun <T> async(dispatcher: CoroutineDispatcher, fullException: Boolean, block: suspend CoroutineScope.() -> T): Deferred<T> {
        return defaultAsyncManager.async(Dispatchers.Unconfined, fullException, block)
    }

    /**
     * Blocking method to make async task.
     * @param T Return object when call is finished
     * @param block Function to execute in asynchronous task
     * @param dispatcher Executor thread
     * @param fullException If its is true, an exception launched on some child task affects to the
     * rest of task, including the parent one, if it is false, only affect to the child class
     */
    override suspend fun <T> asyncAwait(dispatcher: CoroutineDispatcher, fullException: Boolean, block: suspend CoroutineScope.() -> T): T {
        return async(dispatcher, fullException, block).await()
    }

    /**
     * Cancel all async tasks saved in deferred list
     */
    override fun cancelAllAsync() {
        defaultAsyncManager.cancelAllAsync()
    }

    /**
     * Cancel an specific task through its deferred object
     * @param deferred Task to cancel
     */
    override fun cancelAsync(deferred: Deferred<*>) {
        return defaultAsyncManager.cancelAsync(deferred)
    }
}