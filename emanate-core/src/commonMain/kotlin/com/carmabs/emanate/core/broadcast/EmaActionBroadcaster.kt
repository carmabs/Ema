@file:OptIn(ExperimentalAtomicApi::class)

package com.carmabs.emanate.core.broadcast

import com.carmabs.emanate.core.action.EmaAction
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.reflect.KClass
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onSubscription

/**
 * Created by Carlos Mateo Benito on 11/10/2026.
 *
 * Sends actions to the sources and view models listening to their type. The type of the action is the id of the
 * broadcast. An action is delivered at the moment to everyone listening to it. If nobody is listening, it is lost,
 * unless it is broadcast with keepUntilReceived: then it is kept, the last one of each class, and delivered to the
 * first one that listens to it.
 *
 * Each [com.carmabs.emanate.core.model.EmaConfiguration] has one. By default it is [Shared], used by the whole
 * process, so every view model and source can broadcast to each other. Create a new one to isolate the ones that use
 * it, for example the view models of a library from the ones of the application, or the ones of a test from the ones of
 * the tests running in parallel.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
class EmaActionBroadcaster {

    companion object {
        /**
         * The broadcaster of the default configuration, shared by the whole process
         */
        val Shared: EmaActionBroadcaster = EmaActionBroadcaster()
    }

    /**
     * Someone collecting [listen]. The identity tells apart two listeners of the same types.
     */
    private class Listener(private val actionTypes: List<KClass<out EmaAction>>) {
        fun listensTo(action: EmaAction): Boolean = actionTypes.any { it.isInstance(action) }
    }

    /**
     * The listeners and the pending actions change together, so a kept action is never delivered twice nor lost
     */
    private data class State(val listeners: List<Listener>, val pendingActions: List<EmaAction>)

    private val state = AtomicReference(State(emptyList(), emptyList()))

    // Unlimited buffer, so broadcast never suspends nor drops an action while a listener is busy
    private val actions = MutableSharedFlow<EmaAction>(extraBufferCapacity = Channel.UNLIMITED)

    /**
     * Number of listeners, to check that they stop when they are closed
     */
    internal val listenerCount: Int
        get() = state.load().listeners.size

    /**
     * Number of actions kept until someone listens to them
     */
    internal val pendingCount: Int
        get() = state.load().pendingActions.size

    /**
     * Sends [action] to the listeners of its type.
     * @param keepUntilReceived if nobody is listening to it, it is kept and delivered to the first listener of its
     * type, replacing the previous pending action of the same class
     */
    internal fun broadcast(action: EmaAction, keepUntilReceived: Boolean = false) {
        if (!keepUntilReceived) {
            actions.tryEmit(action)
            return
        }
        var listened = false
        updateState { current ->
            listened = current.listeners.any { it.listensTo(action) }
            if (listened) {
                current
            } else {
                current.copy(pendingActions = current.pendingActions.filterNot { it::class == action::class } + action)
            }
        }
        if (listened) actions.tryEmit(action)
    }

    /**
     * Receives the actions of [actionTypes]: first the pending ones and then the ones broadcast while it is collected.
     */
    internal fun listen(actionTypes: List<KClass<out EmaAction>>): Flow<EmaAction> = flow {
        val listener = Listener(actionTypes)
        try {
            actions
                // It is registered once subscribed, so an action listened to is always emitted after it
                .onSubscription { register(listener).forEach { emit(it) } }
                .filter { listener.listensTo(it) }
                .collect { emit(it) }
        } finally {
            updateState { it.copy(listeners = it.listeners - listener) }
        }
    }

    /**
     * Removes the pending actions, so they don't pass from one test to another
     */
    internal fun clearPendingActions() {
        updateState { it.copy(pendingActions = emptyList()) }
    }

    /**
     * Adds [listener] and returns the pending actions it receives, removing them
     */
    private fun register(listener: Listener): List<EmaAction> {
        var received = emptyList<EmaAction>()
        updateState { current ->
            val (listened, notListened) = current.pendingActions.partition { listener.listensTo(it) }
            received = listened
            State(listeners = current.listeners + listener, pendingActions = notListened)
        }
        return received
    }

    private inline fun updateState(transform: (State) -> State) {
        while (true) {
            val current = state.load()
            if (state.compareAndSet(current, transform(current))) return
        }
    }
}
