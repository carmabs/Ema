package com.carmabs.emanate.android.initializer.bundle.strategy

import android.os.Build
import android.os.Bundle
import com.carmabs.emanate.core.action.EmaAction
import java.io.Serializable

/**
 * Created by Carlos Mateo Benito on 19/3/24.
 *
 * <p>
 * Copyright (c) 2024 by Carmabs. All rights reserved.
 * </p>
 *
 * Saves the initializer in a Bundle as a Serializable, for the View system. It can't travel in a route of Compose
 * navigation, which is text: use [BundleSerializerStrategy.kSerialization] there.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
class SerializableBundleStrategy<I> constructor(private val initializerClass: Class<I>) :
    BundleSerializerStrategy where I : EmaAction.Initializer, I : Serializable {
    override fun save(initializer: EmaAction.Initializer, bundle: Bundle) {
        bundle.putSerializable(EmaAction.Initializer.KEY, initializer as Serializable)
    }

    /**
     * Not supported: reading a Serializable from a text that may come from outside the app, like a deep link, isn't safe
     */
    override fun toStringValue(initializer: EmaAction.Initializer): String = throw unsupportedInRoutes()

    /**
     * Not supported: reading a Serializable from a text that may come from outside the app, like a deep link, isn't safe
     */
    override fun fromStringValue(value: String): EmaAction.Initializer = throw unsupportedInRoutes()

    private fun unsupportedInRoutes() = UnsupportedOperationException(
        "Serializable initializers can't travel in a route: use BundleSerializerStrategy.kSerialization"
    )

    override fun restore(bundle: Bundle): EmaAction.Initializer? {
        val initializer: I? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getSerializable(EmaAction.Initializer.KEY, initializerClass)
        } else {
            bundle.getSerializable(EmaAction.Initializer.KEY) as? I
        }
        return initializer
    }
}
