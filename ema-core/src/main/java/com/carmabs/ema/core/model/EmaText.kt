package com.carmabs.ema.core.model

import java.io.Serializable

/**
 * Created by Carlos Mateo Benito on 25/12/21.
 *
 * <p>
 * Copyright (c) 2021 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
sealed class EmaText(open val data: Array<out Any>? = null) : Serializable {

    companion object {
        fun text(text: String, vararg data: Any) = Text(text, data)
        fun id(id: Int, vararg data: Any) = Id(id, data)
        fun plural(id: Int, quantity: Int, vararg data: Any) = Plural(id, quantity, data)
    }

    data class Text(val text: String, override val data: Array<out Any>?) : EmaText(data) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Text

            if (text != other.text) return false
            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = text.hashCode()
            result = 31 * result + data.contentHashCode()
            return result
        }
    }

    data class Id(val id: Int, override val data: Array<out Any>?) : EmaText(data) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            if (!super.equals(other)) return false

            other as Id

            if (id != other.id) return false
            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + id
            result = 31 * result + data.contentHashCode()
            return result
        }

    }

    data class Plural(val id: Int, val quantity: Int, override val data: Array<out Any>?) :
        EmaText(data) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Plural

            if (id != other.id) return false
            if (quantity != other.quantity) return false
            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id
            result = 31 * result + quantity
            result = 31 * result + data.contentHashCode()
            return result
        }
    }
}
