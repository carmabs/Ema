package com.carmabs.ema.android.delegates

import com.carmabs.ema.core.state.EmaBaseState
import kotlin.reflect.KProperty

/**
 * Created by Carlos Mateo Benito on 13/02/2021.
 *
 * <p>
 * Copyright (c) 2021 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
@Suppress("ClassName")
class emaStateDelegate<T>(private val initialStateCreation:()->T) {

    private var state: T? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
         return state?:initialStateCreation.invoke().apply {
             state = this
         }
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
       state = value
    }
}