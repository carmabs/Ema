package com.carmabs.ema.android.di

import com.carmabs.ema.core.broadcast.BroadcastManager
import com.carmabs.ema.core.broadcast.FlowBroadcastManager
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton

/**
 * Created by Carlos Mateo Benito on 3/26/21.
 *
 * <p>
 * Copyright (c) 2021 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
fun emaInjectionModule() = Kodein.Module(name = "EmaInjectionModule") {
    bind<BroadcastManager>() with singleton { FlowBroadcastManager() }
}
