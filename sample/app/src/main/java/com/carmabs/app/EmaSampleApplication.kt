package com.carmabs.app

import android.app.Application
import com.carmabs.app.di.dataModule
import com.carmabs.app.di.uiModule
import com.carmabs.app.di.useCaseModule
import com.carmabs.ema.android.configuration.Android
import com.carmabs.ema.core.Ema
import com.carmabs.ema.core.model.EmaConfiguration
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 *  *<p>
 * Copyright (c) 2020, Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 *
 * Created by: Carlos Mateo Benito on 21/1/19.
 */
class EmaSampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Ema.init(EmaConfiguration.Android)
        startKoin {
            androidContext(this@EmaSampleApplication)
            modules(dataModule, uiModule, useCaseModule)
        }
    }
}
