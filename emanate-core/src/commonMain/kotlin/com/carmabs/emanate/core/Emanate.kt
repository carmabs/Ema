package com.carmabs.emanate.core

import com.carmabs.emanate.core.broadcast.EmaActionBroadcaster
import com.carmabs.emanate.core.model.EmaConfiguration

/**
 * Entry point to configure Emanate. Call [init] once, when the application starts, before any ViewModel
 * is created. If it is not called, the default [EmaConfiguration] is used.
 *
 * The ViewModels and sources use it unless they receive their own configuration, for example in a library, which
 * can't call [init] as it belongs to the application, or in a test.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
object Emanate {

    private var initializedConfiguration: EmaConfiguration? = null

    /**
     * The current configuration. It is the default one until [init] is called.
     */
    val configuration: EmaConfiguration
        get() = initializedConfiguration ?: defaultConfiguration

    private val defaultConfiguration by lazy { EmaConfiguration() }

    /**
     * Sends the actions broadcast by the side effects to the sources listening to their type.
     */
    internal val actionBroadcaster: EmaActionBroadcaster by lazy { EmaActionBroadcaster() }

    /**
     * Sets the configuration of Emanate. It can be called only once.
     */
    fun init(configuration: EmaConfiguration) {
        check(initializedConfiguration == null) { "Emanate can be initialized only once" }
        initializedConfiguration = configuration
    }

    /**
     * Replaces the configuration, for example to run the ViewModels and use cases on the dispatchers of a test. Unlike
     * [init], it can be called any number of times, so call it in the setup of each test. It is internal: the test
     * utilities of emanate-test use it as a friend module.
     */
    internal fun initForTest(configuration: EmaConfiguration) {
        initializedConfiguration = configuration
    }

    /**
     * Removes the configuration so [init] can be called again. Only for tests.
     */
    internal fun reset() {
        initializedConfiguration = null
    }
}
