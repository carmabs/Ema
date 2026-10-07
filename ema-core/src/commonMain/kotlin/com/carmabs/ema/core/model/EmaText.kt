package com.carmabs.ema.core.model

import com.carmabs.ema.core.constants.STRING_EMPTY

/**
 * Created by Carlos Mateo Benito on 25/12/21.
 *
 * <p>
 * Copyright (c) 2021 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
sealed class EmaText(open val data: Array<out Any>? = null) {
    companion object {
        fun text(text: String, vararg data: Any) = Text(text, data)
        fun empty() = Text(STRING_EMPTY)
        fun id(id: Int, vararg data: Any) = Id(id, data)
        fun plural(id: Int, quantity: Int, vararg data: Any) = Plural(id, quantity, data)
        fun composition(vararg texts: EmaText) = Composition(listOf(*texts))
    }

    // The subclasses are regular classes, not data classes: their constructors are not public, and a data
    // class would expose them through copy(). Equality takes the content of the arguments into account.

    class Composition internal constructor(val texts: List<EmaText>) : EmaText(null) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Composition

            return texts == other.texts
        }

        override fun hashCode(): Int = texts.hashCode()

        override fun toString(): String = "Composition(texts=$texts)"
    }

    class Text internal constructor(val text: String, override val data: Array<out Any>? = null) :
        EmaText(data) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Text

            if (text != other.text) return false
            if (!data.sameArguments(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = text.hashCode()
            result = 31 * result + data.argumentsHashCode()
            return result
        }

        override fun toString(): String = "Text(text=$text, data=${data.argumentsToString()})"
    }

    class Id internal constructor(val id: Int, override val data: Array<out Any>? = null) : EmaText(data) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Id

            if (id != other.id) return false
            if (!data.sameArguments(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id
            result = 31 * result + data.argumentsHashCode()
            return result
        }

        override fun toString(): String = "Id(id=$id, data=${data.argumentsToString()})"
    }

    class Plural internal constructor(val id: Int, val quantity: Int, override val data: Array<out Any>?) :
        EmaText(data) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Plural

            if (id != other.id) return false
            if (quantity != other.quantity) return false
            if (!data.sameArguments(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id
            result = 31 * result + quantity
            result = 31 * result + data.argumentsHashCode()
            return result
        }

        override fun toString(): String = "Plural(id=$id, quantity=$quantity, data=${data.argumentsToString()})"
    }

    fun isEmpty(): Boolean = (this is Text) && this.text.isEmpty()
}

// Texts without arguments are the same whether the arguments are null or an empty array
private fun Array<out Any>?.sameArguments(other: Array<out Any>?): Boolean =
    (this ?: emptyArray<Any>()).contentEquals(other ?: emptyArray<Any>())

private fun Array<out Any>?.argumentsHashCode(): Int = if (isNullOrEmpty()) 0 else contentHashCode()

private fun Array<out Any>?.argumentsToString(): String = (this ?: emptyArray<Any>()).contentToString()
