package com.carmabs.ema.android.di

import com.carmabs.ema.core.broadcast.BroadcastManager
import com.carmabs.ema.core.broadcast.FlowBroadcastManager
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton


/**
 * Created by Carlos Mateo Benito on 3/26/21.
 *
 * <p>
 * Copyright (c) 2021 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
fun emaInjectionModule() = DI.Module(name = "EmaInjectionModule") {
    bind<BroadcastManager>() with singleton { FlowBroadcastManager() }
}
