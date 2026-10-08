package com.carmabs.ema.android.initializer.bundle.strategy

import android.os.Bundle
import com.carmabs.ema.core.action.EmaAction
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * Created by Carlos Mateo Benito on 19/3/24.
 *
 * <p>
 * Copyright (c) 2024 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
class KSerializationBundleStrategy<I : EmaAction.Initializer> internal constructor(
    private val serializer: KSerializer<I>
) : BundleSerializerStrategy {
    override fun save(initializer: EmaAction.Initializer, bundle: Bundle) {
        val json = toStringValue(initializer)
        bundle.putString(EmaAction.Initializer.KEY, json)
    }

    override fun toStringValue(initializer: EmaAction.Initializer): String =
        Json.encodeToString(serializer, initializer as I)

    override fun fromStringValue(value: String): EmaAction.Initializer = Json.decodeFromString(serializer, value)

    override fun restore(bundle: Bundle): EmaAction.Initializer? {
        val json = bundle.getString(EmaAction.Initializer.KEY)
        return json?.let { Json.decodeFromString(serializer, it) }
    }
}
