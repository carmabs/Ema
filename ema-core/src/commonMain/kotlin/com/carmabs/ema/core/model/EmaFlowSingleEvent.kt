package com.carmabs.ema.core.model

import com.carmabs.ema.core.constants.INT_ONE
import com.carmabs.ema.core.constants.INT_ZERO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Created by Carlos Mateo Benito on 11/9/22.
 *
 * <p>
 * Copyright (c) 2022 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
fun <T>emaFlowSingleEvent():MutableSharedFlow<T> = MutableSharedFlow(replay = INT_ZERO, extraBufferCapacity = INT_ONE,BufferOverflow.DROP_OLDEST)