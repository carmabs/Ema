package com.carmabs.ema.core.broadcast

import com.carmabs.ema.core.model.emaFlowSingleEvent
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
class EmaFlowBroadcastManager : EmaBroadcastManager {

    private val sharedFlowsMap = hashMapOf<String,MutableSharedFlow<EmaBroadcastEvent<*>>>()

    private fun addFlow(id:String): MutableSharedFlow<EmaBroadcastEvent<*>> {
        return sharedFlowsMap[id] ?:let {
            emaFlowSingleEvent<EmaBroadcastEvent<*>>().apply{
                sharedFlowsMap[id] = this
            }
        }
    }

    override fun <T> sendBroadcastEvent(event: EmaBroadcastEvent<T>) {
        sharedFlowsMap[event.javaClass.name]?.also {
            it.tryEmit(event)
        }
    }

    override suspend fun <T> registerBroadcast(
        clazz: Class<out EmaBroadcastEvent<T&Any>>,
        listener: suspend (T&Any) -> Unit
    ) {
        addFlow(clazz.name).collect{
            listener.invoke(it.data as (T & Any))
        }
    }
}