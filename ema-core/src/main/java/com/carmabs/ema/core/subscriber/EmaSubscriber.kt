package com.carmabs.ema.core.subscriber

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

/**
 * Created by Carlos Mateo Benito on 25/10/24.
 *
 * <p>
 * Copyright (c) 2024 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
abstract class EmaSubscriber<I, O> {

    operator fun invoke(input: I, dispatcher: CoroutineDispatcher = Dispatchers.IO): Flow<O> {
        return useCaseFunction(input).flowOn(dispatcher)
    }


    /**
     * Function to implement by child classes to execute the code associated to data retrieving.
     * It will be executed on background thread
     */
    protected abstract fun useCaseFunction(input: I): Flow<O>
}