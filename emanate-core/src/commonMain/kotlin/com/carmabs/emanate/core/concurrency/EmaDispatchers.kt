package com.carmabs.emanate.core.concurrency

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Dispatchers used by Emanate, set in [com.carmabs.emanate.core.model.EmaConfiguration.dispatchers].
 *
 * @param main where ViewModels run their coroutines by default. `EmaConfiguration.Android(debug)` uses
 * `Dispatchers.Main.immediate`, so the work started from the main thread runs at once, without waiting for the next
 * frame.
 * @param io where use cases run by default: blocking work such as network or disk. It is `Dispatchers.IO` on the JVM
 * and native platforms, and `Dispatchers.Default` on JS and Wasm, which don't have it.
 * @param default for CPU-intensive work, such as processing images.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
data class EmaDispatchers(
    val main: CoroutineDispatcher = Dispatchers.Main,
    val io: CoroutineDispatcher = defaultIoDispatcher(),
    val default: CoroutineDispatcher = Dispatchers.Default
)

/**
 * `Dispatchers.IO` where it exists, `Dispatchers.Default` elsewhere.
 */
internal expect fun defaultIoDispatcher(): CoroutineDispatcher
