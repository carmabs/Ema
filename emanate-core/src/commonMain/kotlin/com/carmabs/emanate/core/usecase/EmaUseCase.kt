package com.carmabs.emanate.core.usecase

import com.carmabs.emanate.core.Emanate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Base class to handle every use case.
 *
 * All the logic associated to data retrieving must be done inside an use case.
 *
 * @param I Input. Must be the model object that the use case can use to make the request
 * @param O Output.Must be the model object that the use case must return
 * @param dispatcher where the use case runs unless another one is passed when it is invoked. By default the io
 * dispatcher of the configuration of Emanate, read when the use case is created; pass the io dispatcher of your own
 * configuration, for example in a library.
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
abstract class EmaUseCase<I, O>(private val dispatcher: CoroutineDispatcher = Emanate.configuration.dispatchers.io) {

    /**
     * Executes a function inside a background thread provided by dispatcher
     * @param dispatcher by default the one of the use case
     * @return the object with the return value
     */
    suspend operator fun invoke(input: I, dispatcher: CoroutineDispatcher = this.dispatcher): O =
        withContext(dispatcher) { useCaseFunction(input) }

    /**
     * Function to implement by child classes to execute the code associated to data retrieving.
     * It will be executed on background thread
     */
    protected abstract suspend fun useCaseFunction(input: I): O
}
