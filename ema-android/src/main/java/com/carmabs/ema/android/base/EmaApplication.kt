package com.carmabs.ema.android.base

import android.app.Application
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware

/**
 *
 * Abstract base class to implement Kodein framework in applicacion context
 * to handle dependency injection
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
abstract class EmaApplication : Application(), KodeinAware {

    override val kodein: Kodein = Kodein {
        import(injectAppModule(this@EmaApplication))
    }

    /**
     * The child classes implement this methods to return the module that provides the app scope objects
     * @param app The app which provide the scope
     * @return The Kodein module which makes the injection
     */
    abstract fun injectAppModule(app:EmaApplication):Kodein.Module
}