package com.carmabs.ema.core.usecase
import com.carmabs.ema.core.concurrency.AsyncManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers

/**
 * Base class to handle every use case.
 *
 * All the logic associated to data retrieving must be done inside an use case.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

/**
 * @param T Must be the model object that the use case must return
 * @constructor An AsyncManager must be provided to handle the background tasks
 */
abstract class EmaUseCase<T>(private val asyncManager: AsyncManager) {

    /**
     * Executes a function inside a background thread provided by AsyncManager
     * @return the deferred object with the return value
     */
    suspend fun execute(): Deferred<T> {
        return asyncManager.async(dispatcher) { useCaseFunction() }
    }

    /**
     * Function to implement by child classes to execute the code associated to data retrieving.
     * It will be executed inside an AsyncTask
     */
    protected abstract suspend fun useCaseFunction(): T

    /**
     * Dispatcher used for useCase execution
     */
    protected open val dispatcher: CoroutineDispatcher = Dispatchers.Main
}