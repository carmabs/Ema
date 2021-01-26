package com.carmabs.ema.core.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * Base class to handle every use case.
 *
 * All the logic associated to data retrieving must be done inside an use case.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

/**
 * @param I Input. Must be the model object that the use case can use to make the request
 * @param O Output.Must be the model object that the use case must return
 */

abstract class EmaUseCase<I, O> {

    /**
     * Executes a function inside a background thread provided by dispatcher
     * @return the object with the return value
     */
    suspend fun execute(input: I): O {
        return withContext(dispatcher) { useCaseFunction(input) }
    }

    /**
     * Executes a function inside a background thread provided by dispatcher blocking the thread until
     * the result is delivered
     * @return the object with the return value
     */
    fun executeSync(input: I): O {
        return runBlocking {
                useCaseFunction(input)
        }
    }


    /**
     * Function to implement by child classes to execute the code associated to data retrieving.
     * It will be executed on background thread
     */
    protected abstract suspend fun useCaseFunction(input: I): O

    /**
     * Dispatcher used for useCase execution
     */
    protected open val dispatcher: CoroutineDispatcher = Dispatchers.IO
}