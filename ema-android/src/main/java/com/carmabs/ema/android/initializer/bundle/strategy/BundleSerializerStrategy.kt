package com.carmabs.ema.android.initializer.bundle.strategy

import android.os.Bundle
import android.os.Parcelable
import com.carmabs.ema.core.action.EmaAction
import com.carmabs.ema.core.constants.STRING_EMPTY
import java.io.Serializable
import kotlinx.serialization.KSerializer

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
    fun save(initializer: EmaAction.Initializer, bundle: Bundle)
    fun toStringValue(initializer: EmaAction.Initializer): String
    fun fromStringValue(value: String): EmaAction.Initializer
    fun restore(bundle: Bundle): EmaAction.Initializer?

    companion object {
        val EMPTY = object : BundleSerializerStrategy {
            override fun save(initializer: EmaAction.Initializer, bundle: Bundle) = Unit

            override fun toStringValue(initializer: EmaAction.Initializer): String = STRING_EMPTY

            override fun fromStringValue(value: String): EmaAction.Initializer = EmaAction.Initializer.EMPTY

            override fun restore(bundle: Bundle): EmaAction.Initializer? = null
        }

        fun <I : EmaAction.Initializer> kSerialization(serializer: KSerializer<I>) =
            KSerializationBundleStrategy(serializer = serializer)

        inline fun <reified I> serializable() where I : EmaAction.Initializer, I : Serializable =
            SerializableBundleStrategy(I::class.java)

        inline fun <reified I> parcelable() where I : EmaAction.Initializer, I : Parcelable =
            ParcelableBundleStrategy(I::class.java)
    }
}
