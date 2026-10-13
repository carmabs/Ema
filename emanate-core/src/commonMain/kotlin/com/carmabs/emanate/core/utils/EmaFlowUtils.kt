package com.carmabs.emanate.core.utils

import com.carmabs.emanate.core.constants.INT_ONE
import com.carmabs.emanate.core.constants.INT_ZERO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Flow utilities. Use them through `EmaUtils.Flows`.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
object EmaFlowUtils {

    /**
     * Creates a flow for one-shot signals: it doesn't replay values and, while nobody collects it, it keeps only the
     * last one.
     */
    fun <T> singleEvent(): MutableSharedFlow<T> =
        MutableSharedFlow(replay = INT_ZERO, extraBufferCapacity = INT_ONE, BufferOverflow.DROP_OLDEST)
}
