package com.carmabs.emanate.android.configuration

import com.carmabs.emanate.android.logging.EmaAndroidDataClassPrinter
import com.carmabs.emanate.core.concurrency.EmaDispatchers
import com.carmabs.emanate.core.model.EmaConfiguration
import com.carmabs.emanate.core.sideeffect.EmaSideEffectConfig
import kotlinx.coroutines.Dispatchers

/**
 * Emanate configuration for Android:
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
