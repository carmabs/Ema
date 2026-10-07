package com.carmabs.ema.core

import com.carmabs.ema.core.model.EmaConfiguration

/**
 * Entry point to configure Ema. Call [init] once, when the application starts, before any ViewModel
 * is created. If it is not called, the default [EmaConfiguration] is used.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
object Ema {

    private var initializedConfiguration: EmaConfiguration? = null

    /**
     * The current configuration. It is the default one until [init] is called.
     */
    val configuration: EmaConfiguration
        get() = initializedConfiguration ?: defaultConfiguration

    private val defaultConfiguration by lazy { EmaConfiguration() }

    /**
     * Sets the configuration of Ema. It can be called only once.
     */
    fun init(configuration: EmaConfiguration) {
        check(initializedConfiguration == null) { "Ema can be initialized only once" }
        initializedConfiguration = configuration
    }
}
