package com.carmabs.ema.core.usecase

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

interface EmaSyncUseCase<I, O> {

    /**
     * Executes the use case function
     * @return the object with the return value
     */
    operator fun invoke(input: I): O

}