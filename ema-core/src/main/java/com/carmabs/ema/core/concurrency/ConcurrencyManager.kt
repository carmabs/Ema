package com.carmabs.ema.core.concurrency

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

/**
 * Manager to handle concurrency tasks
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

interface ConcurrencyManager{

    /**
     * Cancel al pending tasks in process
     */
    fun cancelPendingTasks()

    /**
     * Launch an especific task in the Dispatchers.Main thread by default
     * @param block Function to execute in the thread
     * @param dispatcher Executor thread
     */
    fun launch(dispatcher: CoroutineDispatcher = Dispatchers.Main, block: suspend CoroutineScope.() -> Unit): Job

    /**
     * Cancel a task in process through its job
     * @param job Job to cancel the task in process
     */
    fun cancelTask(job: Job)
}