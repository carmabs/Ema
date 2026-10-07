package com.carmabs.ema.core.concurrency

import com.carmabs.ema.core.Ema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Scope used by ViewModels by default. It runs on [com.carmabs.ema.core.model.EmaConfiguration.mainDispatcher].
 */
@Suppress("FunctionName") // Factory named like the MainScope() of coroutines
fun EmaMainScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Ema.configuration.mainDispatcher)
