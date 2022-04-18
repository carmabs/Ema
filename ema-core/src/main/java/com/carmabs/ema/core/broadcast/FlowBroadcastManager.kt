package com.carmabs.ema.core.broadcast

import com.carmabs.ema.core.constants.INT_ONE
import com.carmabs.ema.core.constants.INT_ZERO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

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

    private val sharedFlow = MutableSharedFlow<EmaBroadcastEvent>(extraBufferCapacity = INT_ONE, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun sendBroadcastEvent(event: EmaBroadcastEvent) {
        sharedFlow.tryEmit(event)
    }

    override suspend fun registerBroadcast(id: String, listener: suspend (Any) -> Unit) {
        sharedFlow.collect {
            if(it.id == id)
                listener.invoke(it.data)
        }
    }
}