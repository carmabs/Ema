package com.carmabs.ema.core.action

import com.carmabs.ema.core.state.EmaEvent
import com.carmabs.ema.core.viewmodel.EmaViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlin.collections.plus

/**
 * Created by Carlos Mateo Benito on 03/08/2026.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
class DefaultEmaEventDispatcher<E : EmaEvent>(private val mEventFlow: MutableStateFlow<List<E>> = MutableStateFlow(emptyList())) : EmaEventDispatcher<E>{

    override fun consumeEvent(event: E) {
        mEventFlow.update { it - event }
    }

    /**
     * Dispatches an effect to be observed by the view.
     * @param event The effect to be dispatched.
     * @param allowDuplicated If true, allows the same effect to be dispatched multiple times before being consumed.
     */
    fun postEvent(event: E, allowDuplicated: Boolean) {
        mEventFlow.update {
            if (allowDuplicated)
                it + event
            else
                if (it.contains(event)) it else it + event
        }
    }

    override val eventFlow: Flow<List<E>> =
        mEventFlow.asStateFlow()
            .distinctUntilChanged { old, new ->
                new.all { old.contains(it) } && old.size >= new.size
            }
            .filter { it.isNotEmpty() }
}