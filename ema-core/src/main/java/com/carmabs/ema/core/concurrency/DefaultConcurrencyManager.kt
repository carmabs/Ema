package com.carmabs.ema.core.concurrency

import kotlinx.coroutines.*

/**
 * ConcurrencyManager implementation for coroutines
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

class DefaultConcurrencyManager : ConcurrencyManager {

    private val jobList: MutableList<Job> = mutableListOf()

    /**
     * Launch an especific task a coroutine Scope an add it to the job list
     * @param block Function to execute in the thread
     * @param dispatcher Executor thread
     * @param fullException If its is true, an exception launched on some child task affects to the
     * rest of task, including the parent one, if it is false, only affect to the child class
     */
    override fun launch(dispatcher: CoroutineDispatcher, fullException: Boolean, block: suspend CoroutineScope.() -> Unit): Job {
        val job = if (fullException) Job() else SupervisorJob()
        val scope = CoroutineScope(dispatcher + job)
        jobList.add(job)
        job.invokeOnCompletion { jobList.remove(job) }
        scope.launch { block.invoke(this) }
        return job
    }


    /**
     * Cancel al pending tasks in job list
     */
    override fun cancelPendingTasks() {
        jobList.forEach { it.cancel() }
        jobList.clear()
    }

    /**
     * Cancel a task in process through its job only if it's on job list
     * @param job Job to cancel the task in process
     */
    override fun cancelTask(job: Job) {
        if (jobList.remove(job)) {
            job.cancel()
        }
    }
}