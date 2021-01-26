package com.carmabs.ema.core.extension

import com.carmabs.ema.core.constants.*

/**
 * Created by Carlos Mateo Benito on 2019-11-24.
 *
 * <p>
 * Copyright (c) 2019 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */

fun Int?.checkNull(defaultValue: Int = INT_ZERO): Int {
    return this ?: defaultValue
}

fun Float?.checkNull(defaultValue: Float = FLOAT_ZERO): Float {
    return this ?: defaultValue
}

fun Short?.checkNull(defaultValue: Short = SHORT_ZERO): Short {
    return this ?: defaultValue
}

fun Long?.checNull(defaultValue: Long = LONG_ZERO): Long {
    return this ?: defaultValue
}

fun Double?.checkNull(defaultValue: Double = DOUBLE_ZERO): Double {
    return this ?: defaultValue
}

fun Boolean?.checkNull(defaultValue: Boolean = false): Boolean {
    return this ?: defaultValue
}

fun Float.constraintValue(minLimit: Float, maxLimit: Float): Float {
    return coerceAtLeast(minLimit).coerceAtMost(maxLimit)
}
