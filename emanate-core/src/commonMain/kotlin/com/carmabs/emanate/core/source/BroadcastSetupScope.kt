package com.carmabs.emanate.core.source

import com.carmabs.emanate.core.action.EmaAction
import kotlin.reflect.KClass

/**
 * Created by Carlos Mateo Benito on 12/10/2026.
 *
 * Block of setup that adds the types of the broadcast actions the source listens to.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
@EmaSourceDsl
class BroadcastSetupScope internal constructor(private val guard: SetupGuard) {

    internal var broadcastListenerList: List<KClass<out EmaAction>> = emptyList()
        private set

    /**
     * Implementation of the inline listen with the type of the actions.
     */
    @PublishedApi
    internal fun listen(actionType: KClass<out EmaAction>) {
        guard.checkNotBuilt()
        broadcastListenerList = broadcastListenerList + actionType
    }

    /**
     * Listens to the actions of type [F] broadcast with the broadcast function of any side effect. The type of the
     * action is the id of the broadcast. They enter this source like the dispatched ones: they go through its
     * middlewares, reducers and side effects of type [F]. The listener is removed when the source is closed.
     */
    inline fun <reified F : EmaAction> listen() {
        listen(F::class)
    }
}
