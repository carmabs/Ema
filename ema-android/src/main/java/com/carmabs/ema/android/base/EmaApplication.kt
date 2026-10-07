package com.carmabs.ema.android.base

import android.app.Application
import androidx.annotation.CallSuper
import com.carmabs.ema.android.configuration.Android
import com.carmabs.ema.core.model.EmaConfiguration

/**
 * Base [Application] that initializes Ema with [emaConfiguration]. If your application already extends
 * another class, implement [EmaApplicationAware] instead.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
abstract class EmaApplication : Application(), EmaApplicationAware {

    /**
     * Ema configuration. By default [EmaConfiguration.Android].
     */
    open val emaConfiguration: EmaConfiguration = EmaConfiguration.Android

    @CallSuper
    override fun onCreate() {
        super.onCreate()
        initializeEma(emaConfiguration)
    }

}