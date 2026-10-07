package com.carmabs.ema.android.base

import android.app.Application
import androidx.annotation.CallSuper
import com.carmabs.ema.core.model.EmaApplicationConfig

/**
 * Base [Application] that initializes Ema with [emaConfiguration]. If your application already extends
 * another class, implement [EmaApplicationAware] instead.
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