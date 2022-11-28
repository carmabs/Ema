package com.carmabs.ema.android.base

import android.app.Application
import androidx.annotation.CallSuper
import com.carmabs.ema.android.di.emaInjectionModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module

/**
 *
 * Abstract base class to implement Kodein framework in applicacion context
 * to handle dependency injection
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
abstract class EmaApplication : Application() {

    @CallSuper
    override fun onCreate() {
        super.onCreate()
        startKoin{
            androidContext(this@EmaApplication)

            val modulesList = mutableListOf<Module>()
            modulesList.add(emaInjectionModule())
            injectAppModules()?.also {
                it.forEach { module->
                    modulesList.add(module)
                }

            }
            modules(modulesList)
        }
    }


    /**
     * The child classes implement this methods to return the module that provides the app scope objects
     * @return The Koin module which makes the injection
     */
    abstract fun injectAppModules(): List<Module>?
}