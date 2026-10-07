@file:OptIn(ExperimentalAtomicApi::class)

package com.carmabs.ema.core.delegate

import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Delegate whose value can be read and written safely from several threads.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
@Suppress("ClassName")
class emaSyncDelegate<T>(defaultValue: T) : ReadWriteProperty<Any, T> {

    private val backingField = AtomicReference(defaultValue)

    override fun getValue(thisRef: Any, property: KProperty<*>): T = backingField.load()

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        backingField.store(value)
    }
}
