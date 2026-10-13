package com.carmabs.emanate.android.initializer.bundle.strategy

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import com.carmabs.emanate.core.action.EmaAction

/**
 * Created by Carlos Mateo Benito on 19/3/24.
 *
 * <p>
 * Copyright (c) 2024 by Carmabs. All rights reserved.
 * </p>
 *
 * Saves the initializer in a Bundle as a Parcelable, for the View system. It can't travel in a route of Compose
 * navigation, which is text: use [BundleSerializerStrategy.kSerialization] there.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
class ParcelableBundleStrategy<I> constructor(private val initializerClass: Class<I>) :
    BundleSerializerStrategy where I : EmaAction.Initializer, I : Parcelable {
    override fun save(initializer: EmaAction.Initializer, bundle: Bundle) {
        bundle.putParcelable(EmaAction.Initializer.KEY, initializer as Parcelable)
    }

    /**
     * Not supported: reading a Parcelable from a text that may come from outside the app, like a deep link, isn't safe
     */
    override fun toStringValue(initializer: EmaAction.Initializer): String = throw unsupportedInRoutes()

    /**
     * Not supported: reading a Parcelable from a text that may come from outside the app, like a deep link, isn't safe
     */
    override fun fromStringValue(value: String): EmaAction.Initializer = throw unsupportedInRoutes()

    private fun unsupportedInRoutes() = UnsupportedOperationException(
        "Parcelable initializers can't travel in a route: use BundleSerializerStrategy.kSerialization"
    )

    override fun restore(bundle: Bundle): EmaAction.Initializer? {
        val initializer: I? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelable(EmaAction.Initializer.KEY, initializerClass)
        } else {
            bundle.getParcelable(EmaAction.Initializer.KEY) as? I
        }
        return initializer
    }
}
