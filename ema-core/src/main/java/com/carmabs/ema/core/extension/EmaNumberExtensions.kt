package com.carmabs.ema.core.extension

import com.carmabs.ema.core.constants.*
import kotlin.math.ceil

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

fun Int.constraintValue(minLimit: Int, maxLimit: Int): Int {
    return coerceAtLeast(minLimit).coerceAtMost(maxLimit)
}

/**
 * Method to get index interpolating from center to alternatives vector ends
 */
fun iteratePositionFromCenter(index: Int, length: Int, startRight: Boolean = true): Int {
    val centerPosition = length / 2
    val position = when {
        index <= INT_ZERO -> {
            centerPosition
        }
        index >= length -> {
            centerPosition
        }
        else -> {
            val directionFactor = if (index % 2 == INT_ZERO) {
                if (startRight) INT_ONE else -INT_ONE
            } else
                if (startRight) -INT_ONE else INT_ONE

            val coefficient = ((index / 3) + INT_ONE) * directionFactor
            centerPosition + coefficient
        }
    }
    return position
}

/**
 * Method to get index interpolating alternatives vector ends  to center
 */
fun iteratePositionFromEnd(index: Int, length: Int, startRight: Boolean = true): Int {
    val centerPosition = length / 2
    val position = when {
        index < INT_ZERO -> {
            centerPosition
        }
        index >= length -> {
            centerPosition
        }
        else -> {
            val right = if ((index+ INT_ONE) % 2 != INT_ZERO) {
                startRight
            } else
                !startRight

            val coefficient = (index / 2)

            if (right) {
                length - coefficient - INT_ONE
            } else
                coefficient

        }
    }
    return position
}
