package com.carmabs.ema.android.configuration

import com.carmabs.ema.android.logging.EmaAndroidDataClassPrinter
import com.carmabs.ema.core.concurrency.EmaDispatchers
import com.carmabs.ema.core.model.EmaConfiguration
import com.carmabs.ema.core.model.EmaSideEffectConfig
import kotlinx.coroutines.Dispatchers

/**
 * Ema configuration for Android:
 * - ViewModels run on [Dispatchers.Main.immediate], so the work started from the main thread runs at once, and use
 *   cases on `Dispatchers.IO`.
 * - The method that launched a sideEffect is resolved from its lambda.
 * - Objects are pretty printed with their fields.
 *
 * Customize it with `copy`, for example `EmaConfiguration.Android.copy(sideEffectConfig = ...)`.
 */
val EmaConfiguration.Companion.Android: EmaConfiguration
    get() = EmaConfiguration(
        dispatchers = EmaDispatchers(main = Dispatchers.Main.immediate),
        sideEffectConfig = EmaSideEffectConfig(methodNameResolver = EmaAndroidMethodNameResolver),
        dataClassPrinter = EmaAndroidDataClassPrinter()
    )
