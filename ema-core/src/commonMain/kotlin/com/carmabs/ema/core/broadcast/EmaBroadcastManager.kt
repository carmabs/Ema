package com.carmabs.ema.core.broadcast

import kotlin.reflect.KClass

/**
 * Created by Carlos Mateo Benito on 25/02/2021.
 *
 * <p>
 * Copyright (c) 2021 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
interface EmaBroadcastManager {
    fun <T> sendBroadcastEvent(event: EmaBroadcastEvent<T>)
    suspend fun <T> registerBroadcast(clazz: KClass<out EmaBroadcastEvent<T&Any>>, listener: suspend (T&Any) -> Unit)
}
