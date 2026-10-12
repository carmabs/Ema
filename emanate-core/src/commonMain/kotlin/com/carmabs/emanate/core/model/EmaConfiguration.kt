package com.carmabs.emanate.core.model

import com.carmabs.emanate.core.concurrency.EmaDispatchers
import com.carmabs.emanate.core.logging.EmaDataClassPrinter
import com.carmabs.emanate.core.sideeffect.EmaSideEffectConfig

/**
 * Configuration of Ema, set with [com.carmabs.emanate.core.Ema.init].
 *
 * @param dispatchers where ViewModels ([EmaDispatchers.main]), use cases ([EmaDispatchers.io]) and CPU-intensive work
 * ([EmaDispatchers.default]) run by default
 * @param sideEffectConfig behaviour of sideEffect in ViewModels
 * @param dataClassPrinter used by [com.carmabs.emanate.core.logging.toStringPretty]
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
data class EmaConfiguration(
    val dispatchers: EmaDispatchers = EmaDispatchers(),
    val sideEffectConfig: EmaSideEffectConfig = EmaSideEffectConfig(),
    val dataClassPrinter: EmaDataClassPrinter = EmaDataClassPrinter.ToString
) {
    // Empty, so other modules can add predefined configurations as extensions, like EmaConfiguration.Android
    companion object
}
