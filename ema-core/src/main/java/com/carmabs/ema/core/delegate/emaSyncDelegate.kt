package com.carmabs.ema.core.delegate

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by Carlos Mateo Benito on 16/8/22.
 *
 * <p>
 * Copyright (c) 2022 by Carmabs. All rights reserved.
 * </p>
 * Delegate to make a var synchronized
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
class emaSyncDelegate<T>(defaultValue: T) : ReadWriteProperty<Any, T> {

    private var backingField = defaultValue

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return synchronized(this) {
            backingField
        }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        synchronized(this) {
            backingField = value
        }
    }
}