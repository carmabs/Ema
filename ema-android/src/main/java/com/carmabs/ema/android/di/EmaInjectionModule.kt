package com.carmabs.ema.android.di

import com.carmabs.ema.core.broadcast.EmaBroadcastManager
import com.carmabs.ema.core.broadcast.EmaFlowBroadcastManager
import org.koin.dsl.module


/**
 * Created by Carlos Mateo Benito on 3/26/21.
 *
 * <p>
 * Copyright (c) 2021 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
internal fun emaInjectionModule() = module {
    single<EmaBroadcastManager> { EmaFlowBroadcastManager() }
}
