package com.carmabs.ema.android.base

import android.app.Application
import androidx.annotation.CallSuper
import com.carmabs.ema.core.model.EmaApplicationConfig

/**
 *
 * Abstract base class to implement Kodein framework in applicacion context
 * to handle dependency injection
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
abstract class EmaApplication : Application(), EmaApplicationAware {

    abstract val emaConfiguration: EmaApplicationConfig

    @CallSuper
    override fun onCreate() {
        super.onCreate()
        initializeEma(emaConfiguration)
    }

}