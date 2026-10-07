package com.carmabs.ema.core.model

import com.carmabs.ema.core.logging.EmaDataClassPrinter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Configuration of Ema, set with [com.carmabs.ema.core.Ema.init].
 *
 * @param mainDispatcher dispatcher where ViewModels run their coroutines by default
 * @param useCaseBackgroundDispatcher dispatcher where use cases run by default
 * @param sideEffectConfig behaviour of sideEffect in ViewModels
 * @param dataClassPrinter used by [com.carmabs.ema.core.logging.toStringPretty]
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
data class EmaConfiguration(
    val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    val useCaseBackgroundDispatcher: CoroutineDispatcher = Dispatchers.Default,
    val sideEffectConfig: EmaSideEffectConfig = EmaSideEffectConfig(),
    val dataClassPrinter: EmaDataClassPrinter = EmaDataClassPrinter.ToString
) {
    companion object
}
