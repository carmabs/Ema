package com.carmabs.emanate.core.logging

import com.carmabs.emanate.core.Emanate

/**
 * Prints objects in a readable way for debugging. Configure the implementation with
 * [com.carmabs.emanate.core.model.EmaConfiguration.printer].
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
 * Prints the object with [printer].
 *
 * @param printer by default the one of the current Emanate configuration. Pass the one of your own configuration, for
 * example in a library.
 */
fun Any?.toStringPretty(printer: EmaDataClassPrinter = Emanate.configuration.printer): String = printer.print(this)
