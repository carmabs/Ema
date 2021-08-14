package com.carmabs.ema.presentation.injection

import android.app.Application
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

/**
 *  *<p>
 * Copyright (c) 2020, Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 *
 * Created by: Carlos Mateo Benito on 20/1/19.
 */

fun appInjection(application: Application) = DI.Module(name = "AppModule") {

    bind<Application>() with singleton { application }

}