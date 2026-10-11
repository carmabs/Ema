@file:OptIn(ExperimentalAtomicApi::class)

package com.carmabs.emanate.core.broadcast

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.action.EmaActionDispatcher
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.reflect.KClass

/**
 * Created by Carlos Mateo Benito on 11/10/2026.
 *
 * Sends actions to the sources listening to their type, added with addBroadcastListener. The type of the action is
 * the id of the broadcast. It is delivered at the moment to the listeners added: if nobody is listening, it is lost.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
internal class EmaActionBroadcaster : EmaActionDispatcher<EmaAction> {

    /**
     * Receives the actions of [actionType]
     */
    class Listener(val actionType: KClass<out EmaAction>, val onAction: (EmaAction) -> Unit)

    private val listeners = AtomicReference<List<Listener>>(emptyList())

    /**
     * Number of listeners added, to check that they are removed
     */
    val listenerCount: Int
        get() = listeners.load().size

    fun addListener(listener: Listener) {
        updateListeners { it + listener }
    }

    fun removeListener(listener: Listener) {
        updateListeners { it - listener }
    }

    /**
     * Sends [action] to the listeners of its type
     */
    override fun dispatch(action: EmaAction) {
        listeners.load().forEach { listener ->
            if (listener.actionType.isInstance(action)) listener.onAction(action)
        }
    }

    private inline fun updateListeners(transform: (List<Listener>) -> List<Listener>) {
        while (true) {
            val current = listeners.load()
            if (listeners.compareAndSet(current, transform(current))) return
        }
    }
}
