package com.carmabs.ema.core.delegate

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by Carlos Mateo Benito on 12/03/2021.
 *
 * <p>
 * Copyright (c) 2021 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
class emaBooleanDelegate<T>(initialValue:Boolean) : ReadWriteProperty<T,Boolean> {

    private var booleanValue: Boolean = initialValue

    override fun setValue(thisRef: T, property: KProperty<*>, value: Boolean) {
        booleanValue = value
    }

    override fun getValue(thisRef: T, property: KProperty<*>): Boolean {
        return booleanValue
    }


}