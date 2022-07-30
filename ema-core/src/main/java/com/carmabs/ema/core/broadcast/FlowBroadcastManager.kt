package com.carmabs.ema.core.broadcast

import com.carmabs.ema.core.constants.INT_ONE
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Created by Carlos Mateo Benito on 25/02/2021.
 *
 * <p>
 * Copyright (c) 2021 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
class FlowBroadcastManager : BroadcastManager {

    private val sharedFlow = MutableSharedFlow<EmaBroadcastEvent<*>>(extraBufferCapacity = INT_ONE, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun <T> sendBroadcastEvent(event: EmaBroadcastEvent<T>) {
        sharedFlow.tryEmit(event)
    }

    override suspend fun <T> registerBroadcast(
        clazz: Class<out EmaBroadcastEvent<T>>,
        listener: suspend (EmaBroadcastEvent<T>) -> Unit
    ) {
        sharedFlow.collect {
            if(clazz.name == it.javaClass.name)
                listener.invoke(it as EmaBroadcastEvent<T>)
        }
    }
}