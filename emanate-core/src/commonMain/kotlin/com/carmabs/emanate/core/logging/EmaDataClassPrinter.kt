package com.carmabs.emanate.core.logging

import com.carmabs.emanate.core.Emanate

/**
 * Prints objects in a readable way for debugging. Configure the implementation with
 * [com.carmabs.emanate.core.model.EmaConfiguration.dataClassPrinter].
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
 * Prints the object with the [EmaDataClassPrinter] of the current Emanate configuration.
 */
fun Any?.toStringPretty(): String = Emanate.configuration.dataClassPrinter.print(this)
