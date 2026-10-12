package com.carmabs.emanate.core.broadcast

import com.carmabs.emanate.core.action.EmaAction
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Created by Carlos Mateo Benito on 11/10/2026.
 *
 * Sends actions to the sources listening to their type, added with addBroadcastListener. The type of the action is
 * the id of the broadcast. It is delivered at the moment to the sources collecting [actions]: if nobody is
 * collecting, it is lost.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
internal class EmaActionBroadcaster {

    // Unlimited buffer, so broadcast never suspends nor drops an action while a source is busy
    private val mutableActions = MutableSharedFlow<EmaAction>(extraBufferCapacity = Channel.UNLIMITED)

    /**
     * Every action broadcast. Each source filters the types it listens to.
     */
    val actions: SharedFlow<EmaAction> = mutableActions.asSharedFlow()

    /**
     * Number of sources collecting [actions], to check that they stop when they are closed
     */
    val listenerCount: Int
        get() = mutableActions.subscriptionCount.value

    /**
     * Sends [action] to the sources listening to its type
     */
    fun broadcast(action: EmaAction) {
        mutableActions.tryEmit(action)
    }
}
