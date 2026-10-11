package com.carmabs.emanate.core.concurrency

import com.carmabs.emanate.core.Ema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Scope used by ViewModels by default. It runs on the [EmaDispatchers.main] of the configuration.
 */
@Suppress("FunctionName") // Factory named like the MainScope() of coroutines
fun EmaMainScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Ema.configuration.dispatchers.main)
