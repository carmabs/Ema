package com.carmabs.ema.core.concurrency

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Dispatchers used by Ema, set in [com.carmabs.ema.core.model.EmaConfiguration.dispatchers].
 *
 * @param main where ViewModels run their coroutines by default. It is `Dispatchers.Main.immediate` where the platform
 * supports it, so the work started from the main thread runs at once, without waiting for the next frame, and
 * `Dispatchers.Main` where it doesn't.
 * @param io where use cases run by default: blocking work such as network or disk. It is `Dispatchers.IO` on the JVM
 * and native platforms, and `Dispatchers.Default` on JS and Wasm, which don't have it.
 * @param default for CPU-intensive work, such as processing images.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
data class EmaDispatchers(
    val main: CoroutineDispatcher = defaultMainDispatcher(),
    val io: CoroutineDispatcher = defaultIoDispatcher(),
    val default: CoroutineDispatcher = Dispatchers.Default
)

/**
 * `Dispatchers.Main.immediate`, or `Dispatchers.Main` where immediate isn't supported or there is no main dispatcher.
 * In that case it fails when used, as `Dispatchers.Main` does.
 */
private fun defaultMainDispatcher(): CoroutineDispatcher = try {
    Dispatchers.Main.immediate
} catch (_: UnsupportedOperationException) {
    Dispatchers.Main
} catch (_: IllegalStateException) {
    Dispatchers.Main
}

/**
 * `Dispatchers.IO` where it exists, `Dispatchers.Default` elsewhere.
 */
internal expect fun defaultIoDispatcher(): CoroutineDispatcher
