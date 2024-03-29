package com.carmabs.ema.android.initializer.bundle.strategy

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import com.carmabs.ema.core.initializer.EmaInitializer

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
    BundleSerializerStrategy where I : EmaInitializer, I : Parcelable {
    override fun save(initializer: EmaInitializer, bundle: Bundle) {
        bundle.putParcelable(EmaInitializer.KEY, initializer as Parcelable)
    }

    override fun toStringValue(initializer: EmaInitializer): String {
        //TODO Check if should be recommendable pass string parser as argument
        return initializer.toString()
    }

    override fun fromStringValue(value: String): EmaInitializer {
        //TODO Check if should be recommendable pass string parser as argument
        return EmaInitializer.EMPTY
    }

    override fun restore(bundle: Bundle): EmaInitializer? {
        val initializer: I? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelable(EmaInitializer.KEY, initializerClass)
        } else {
            bundle.getParcelable(EmaInitializer.KEY) as? I

        }
        return initializer
    }
}