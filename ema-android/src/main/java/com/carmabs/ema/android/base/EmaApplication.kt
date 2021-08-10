package com.carmabs.ema.android.base

import android.app.Application
import com.carmabs.ema.android.di.Injector
import com.carmabs.ema.android.di.emaInjectionModule
import org.kodein.di.DI
import org.kodein.di.android.x.androidXModule

/**
 *
 * Abstract base class to implement Kodein framework in applicacion context
 * to handle dependency injection
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
abstract class EmaApplication : Application(), Injector {

    override val parentKodein: DI= DI {
        androidXModule(this@EmaApplication)
    }

    override val di: DI = injectKodein()

    final override fun injectModule(kodeinBuilder: DI.MainBuilder): DI.Module? {
        kodeinBuilder.import(emaInjectionModule())
        return injectAppModule(kodeinBuilder)
    }


    /**
     * The child classes implement this methods to return the module that provides the app scope objects
     * @param kodein The kodein object which provide the injection
     * @return The Kodein module which makes the injection
     */
    abstract fun injectAppModule(kodeinBuilder:DI.MainBuilder):DI.Module?
}