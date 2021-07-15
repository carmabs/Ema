package com.carmabs.ema.core.broadcast

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

    private val sharedFlow = MutableSharedFlow<EmaBroadcastEvent>()

    override suspend fun sendBroadcastEvent(event: EmaBroadcastEvent) {
        sharedFlow.emit(event)
    }

    override suspend fun registerBroadcast(id: String, listener: suspend (Any) -> Unit) {
        sharedFlow.collect {
            if(it.id == id)
                listener.invoke(it.data)
        }
    }
}