package com.carmabs.emanate.compose.initializer

import com.carmabs.emanate.android.initializer.bundle.strategy.BundleSerializerStrategy
import com.carmabs.emanate.android.initializer.bundle.strategy.KSerializationBundleStrategy
import com.carmabs.emanate.core.action.EmaAction
import kotlinx.serialization.KSerializer

/**
 * Created by Carlos Mateo Benito on 20/3/24.
 *
 * <p>
 * Copyright (c) 2024 by Carmabs. All rights reserved.
 * </p>
 *
 * How a destination of Compose navigation reads its initializer. It travels in the route as text, so it is read
 * with kotlinx.serialization: the Parcelable and Serializable strategies only work with Bundles.
 *
 * @param overrideInitializer replaces the initializer of the route, for example the one of the intent of the activity
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
open class EmaInitializerSupport(
    val serializerStrategy: KSerializationBundleStrategy<*>,
    val overrideInitializer: EmaAction.Initializer? = null
) {
    companion object {
        fun <I : EmaAction.Initializer> kSerialization(serializer: KSerializer<I>, overrideInitializer: I? = null) =
            EmaInitializerSupport(
                BundleSerializerStrategy.kSerialization(serializer),
                overrideInitializer
            )
    }
}
