package com.carmabs.ema.core.model

import com.carmabs.ema.core.concurrency.EmaDispatchers
import com.carmabs.ema.core.logging.EmaDataClassPrinter

/**
 * Configuration of Ema, set with [com.carmabs.ema.core.Ema.init].
 *
 * @param dispatchers where ViewModels ([EmaDispatchers.main]), use cases ([EmaDispatchers.io]) and CPU-intensive work
 * ([EmaDispatchers.default]) run by default
 * @param sideEffectConfig behaviour of sideEffect in ViewModels
 * @param dataClassPrinter used by [com.carmabs.ema.core.logging.toStringPretty]
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
data class EmaConfiguration(
    val dispatchers: EmaDispatchers = EmaDispatchers(),
    val sideEffectConfig: EmaSideEffectConfig = EmaSideEffectConfig(),
    val dataClassPrinter: EmaDataClassPrinter = EmaDataClassPrinter.ToString
) {
    companion object
}
