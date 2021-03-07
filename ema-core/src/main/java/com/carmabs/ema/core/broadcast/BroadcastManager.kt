package com.carmabs.ema.core.broadcast

/**
 * Created by Carlos Mateo Benito on 25/02/2021.
 *
 * <p>
 * Copyright (c) 2021 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
interface BroadcastManager {
   suspend fun sendBroadcastEvent(event:EmaBroadcastEvent)
   suspend fun registerBroadcast(id: String, listener: (Any) -> Unit)
}