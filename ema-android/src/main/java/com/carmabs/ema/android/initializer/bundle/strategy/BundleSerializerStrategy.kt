package com.carmabs.ema.android.initializer.bundle.strategy

import android.os.Bundle
import android.os.Parcelable
import com.carmabs.ema.core.constants.STRING_EMPTY
import com.carmabs.ema.core.initializer.EmaInitializer
import kotlinx.serialization.KSerializer
import java.io.Serializable

/**
 * Created by Carlos Mateo Benito on 19/3/24.
 *
 * <p>
 * Copyright (c) 2024 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
interface BundleSerializerStrategy {
    fun save(initializer: EmaInitializer, bundle: Bundle)
    fun toStringValue(initializer: EmaInitializer): String
    fun fromStringValue(value: String): EmaInitializer
    fun restore(bundle: Bundle): EmaInitializer?

    companion object {
        val EMPTY = object : BundleSerializerStrategy {
            override fun save(initializer: EmaInitializer, bundle: Bundle) = Unit

            override fun toStringValue(initializer: EmaInitializer): String = STRING_EMPTY

            override fun fromStringValue(value: String): EmaInitializer = EmaInitializer.EMPTY

            override fun restore(bundle: Bundle): EmaInitializer? = null
        }

        fun <I : EmaInitializer> kSerialization(serializer: KSerializer<I>) =
            KSerializationBundleStrategy(serializer = serializer)

        inline fun <reified I> serializable() where I : EmaInitializer, I : Serializable =
            SerializableBundleStrategy(I::class.java)

        inline fun <reified I> parcelable() where I : EmaInitializer, I : Parcelable =
            ParcelableBundleStrategy(I::class.java)
    }
}