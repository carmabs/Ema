package com.carmabs.ema.android.configuration

import com.carmabs.ema.android.logging.EmaAndroidDataClassPrinter
import com.carmabs.ema.core.model.EmaConfiguration
import com.carmabs.ema.core.model.EmaSideEffectConfig

/**
 * Ema configuration for Android:
 * - ViewModels run on `Dispatchers.Main.immediate` and use cases on `Dispatchers.IO`, the default dispatchers.
 * - The method that launched a sideEffect is resolved from its lambda.
 * - Objects are pretty printed with their fields.
 *
 * Customize it with `copy`, for example `EmaConfiguration.Android.copy(sideEffectConfig = ...)`.
 */
val EmaConfiguration.Companion.Android: EmaConfiguration
    get() = EmaConfiguration(
        sideEffectConfig = EmaSideEffectConfig(methodNameResolver = EmaAndroidMethodNameResolver),
        dataClassPrinter = EmaAndroidDataClassPrinter()
    )
