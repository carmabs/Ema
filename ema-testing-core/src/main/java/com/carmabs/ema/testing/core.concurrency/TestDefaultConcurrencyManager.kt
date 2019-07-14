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
     * Launch an especific task a coroutine Scope an add it to the job list
     * @param block Function to execute in the thread
     * @param dispatcher Executor thread
     * @param fullException If its is true, an exception launched on some child task affects to the
     * rest of task, including the parent one, if it is false, only affect to the child class
     */
    override fun launch(dispatcher: CoroutineDispatcher, fullException: Boolean, block: suspend CoroutineScope.() -> Unit): Job {
        return defaultConcurrencyManager.launch (Dispatchers.Unconfined,fullException,block)
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