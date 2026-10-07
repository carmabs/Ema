package com.carmabs.ema.core.model

import com.carmabs.ema.core.value.EmaUriRes

/**
 * Created by Carlos Mateo Benito on 25/12/21.
 *
 * <p>
 * Copyright (c) 2021 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
sealed class EmaImage(open val width: Int? = null, open val height: Int? = null, open val colorTint: Int? = null) {

    data class ByteArray(
        val bytes: kotlin.ByteArray,
        override val width: Int? = null,
        override val height: Int? = null,
        override val colorTint: Int? = null
    ) : EmaImage() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as ByteArray

            if (!bytes.contentEquals(other.bytes)) return false

            return true
        }

        override fun hashCode(): Int = bytes.contentHashCode()
    }

    data class Uri(
        val uri: EmaUriRes,
        override val width: Int? = null,
        override val height: Int? = null,
        override val colorTint: Int? = null
    ) : EmaImage()

    data class Id(
        val id: Int,
        override val width: Int? = null,
        override val height: Int? = null,
        override val colorTint: Int? = null
    ) : EmaImage()
}
