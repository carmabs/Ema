package com.carmabs.emanate.core

import com.carmabs.emanate.core.model.EmaConfiguration
import kotlinx.coroutines.CoroutineDispatcher

/**
 * Returns [configuration] with [dispatcher] as its main dispatcher, where the view models and the sources of the tests
 * run. By default the configuration of Emanate when it is called, like the view models and the sources.
 */
internal fun configurationOn(
    dispatcher: CoroutineDispatcher,
    configuration: EmaConfiguration = Emanate.configuration
): EmaConfiguration = configuration.copy(dispatchers = configuration.dispatchers.copy(main = dispatcher))
