package com.carmabs.ema.android.base

import android.app.Application
import com.carmabs.ema.android.di.emaInjectionModule
import com.carmabs.ema.android.configuration.Android
import com.carmabs.ema.core.Ema
import com.carmabs.ema.core.model.EmaConfiguration
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module

/**
 * Initializes Ema without extending [EmaApplication]. Implement it in your own [Application] and call
 * [initializeEma] from [Application.onCreate]. It starts Koin with the Ema module and the modules
 * returned by [injectAppModules].
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
interface EmaApplicationAware {

    fun Application.initializeEma(config: EmaConfiguration = EmaConfiguration.Android){
        Ema.init(config)
        startKoin{
            androidContext(this@initializeEma)

            val modulesList = mutableListOf<Module>()
            modulesList.add(emaInjectionModule())
            injectAppModules()?.onEach { module->
                modulesList.add(module)
            }
            modules(modulesList)
        }
    }

    /**
     * The child classes implement this methods to return the module that provides the app scope objects
     * @return The Koin module which makes the injection
     */
    fun KoinApplication.injectAppModules(): List<Module>?
}