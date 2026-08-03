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
interface EmaEventDispatcher<E : EmaEvent> {

    fun consumeEvent(event: E)

    val eventFlow: Flow<List<E>>
}