package com.carmabs.ema.core.logging

import com.carmabs.ema.core.Ema

/**
 * Prints objects in a readable way for debugging. Configure the implementation with
 * [com.carmabs.ema.core.model.EmaConfiguration.dataClassPrinter].
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
fun interface EmaDataClassPrinter {

    fun print(value: Any?): String

    companion object {
        /**
         * Uses [toString]. Data classes already print all their properties.
         */
        val ToString = EmaDataClassPrinter { it.toString() }
    }
}

/**
 * Prints the object with the [EmaDataClassPrinter] of the current Ema configuration.
 */
fun Any?.toStringPretty(): String = Ema.configuration.dataClassPrinter.print(this)
