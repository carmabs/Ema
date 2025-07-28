package com.carmabs.ema.core.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
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

abstract class EmaFlowUseCase<I, O>  {

    /**
     * Executes a function inside a background thread provided by dispatcher
     * @return the object with the return value
     */
    operator fun invoke(input: I, dispatcher: CoroutineDispatcher = Dispatchers.IO): Flow<O> {
        return useCaseFunction(input).flowOn(dispatcher)
    }

    /**
     * Function to implement by child classes to execute the code associated to data retrieving.
     * It will be executed on background thread
     */
    protected abstract fun useCaseFunction(input: I): Flow<O>
}