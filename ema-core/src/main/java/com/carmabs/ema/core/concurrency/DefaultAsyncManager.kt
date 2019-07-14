package com.carmabs.ema.core.concurrency

import kotlinx.coroutines.*

/**
 * AsyncManager implementation for coroutines
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

class DefaultAsyncManager : AsyncManager {


    private val deferredList: MutableList<Deferred<*>> = mutableListOf()

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
        val job = if (fullException) Job() else SupervisorJob()
        val deferred: Deferred<T> = CoroutineScope(dispatcher + job).async { block() }
        deferredList.add(deferred)
        deferred.invokeOnCompletion { deferredList.remove(deferred) }
        return deferred
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
        //Create new list to avoid ConcurrentModificationException due to invokeOnCompletion

        val jobPending = mutableListOf<Job>()
        jobPending.addAll(deferredList)
        jobPending.forEach { if (it.isActive) it.cancel() }

        deferredList.clear()
    }

    /**
     * Cancel an specific task through its deferred object
     * @param deferred Task to cancel
     */
    override fun cancelAsync(deferred: Deferred<*>) {
        if (deferredList.remove(deferred))
            deferred.cancel()
    }
}