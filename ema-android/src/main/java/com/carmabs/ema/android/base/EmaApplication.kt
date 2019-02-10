package com.carmabs.ema.android.base

import android.app.Application
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.androidModule

/**
 *
 * Abstract base class to implement Kodein framework in applicacion context
 * to handle dependency injection
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
abstract class EmaApplication : Application(), KodeinAware {

    override val kodein: Kodein = Kodein {
        injectAppModule(this)?.let {
            import(it)
        }?: androidModule(this@EmaApplication)
    }

    /**
     * The child classes implement this methods to return the module that provides the app scope objects
     * @param kodein The object which provide the injection
     * @return The Kodein module which makes the injection
     */
    abstract fun injectAppModule(kodein: Kodein.MainBuilder): Kodein.Module?
}