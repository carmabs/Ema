package com.carmabs.ema.testing.core.concurrency

import com.carmabs.ema.core.concurrency.ConcurrencyManager
import com.carmabs.ema.core.concurrency.DefaultConcurrencyManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

/**
 * ConcurrencyManager testing implementation for coroutines
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
class TestDefaultConcurrencyManager : ConcurrencyManager {

    private val defaultConcurrencyManager = DefaultConcurrencyManager()

    /**
     * Launch an especific task a coroutine Scope an add it to the job list. Same behaviour as default concurrency manager but using
     * Unconfined thread
     * @param block Function to execute in the thread
     * @param dispatcher Executor thread
     */
    override fun launch(dispatcher: CoroutineDispatcher, block: suspend CoroutineScope.() -> Unit): Job {
        return defaultConcurrencyManager.launch (Dispatchers.Unconfined,block)
    }


    /**
     * Cancel al pending tasks in job list
     */
    override fun cancelPendingTasks() {
      defaultConcurrencyManager.cancelPendingTasks()
    }

    /**
     * Cancel a task in process through its job only if it's on job list
     * @param job Job to cancel the task in process
     */
    override fun cancelTask(job: Job) {
       defaultConcurrencyManager.cancelTask(job)
    }
}