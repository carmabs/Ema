package com.carmabs.ema.android.initializer.bundle.strategy

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import com.carmabs.ema.core.action.EmaAction

/**
 * Created by Carlos Mateo Benito on 19/3/24.
 *
 * <p>
 * Copyright (c) 2024 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
class ParcelableBundleStrategy<I> constructor(private val initializerClass: Class<I>) :
    BundleSerializerStrategy where I : EmaAction.Initializer, I : Parcelable {
    override fun save(initializer: EmaAction.Initializer, bundle: Bundle) {
        bundle.putParcelable(EmaAction.Initializer.KEY, initializer as Parcelable)
    }

    override fun toStringValue(initializer: EmaAction.Initializer): String {
        // TODO Check if should be recommendable pass string parser as argument
        return initializer.toString()
    }

    override fun fromStringValue(value: String): EmaAction.Initializer {
        // TODO Check if should be recommendable pass string parser as argument
        return EmaAction.Initializer.EMPTY
    }

    override fun restore(bundle: Bundle): EmaAction.Initializer? {
        val initializer: I? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelable(EmaAction.Initializer.KEY, initializerClass)
        } else {
            bundle.getParcelable(EmaAction.Initializer.KEY) as? I
        }
        return initializer
    }
}
