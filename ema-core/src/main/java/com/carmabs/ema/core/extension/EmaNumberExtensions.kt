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

/**
 * Check if value is greater or equal than min or return null
 */
fun Int.minOrNull(min: Int): Int? {
    return if(this>=min)
        this
    else
        null
}

/**
 * Check if value is smaller or equal than max or return null
 */
fun Int.maxOrNull(max: Int): Int? {
    return if(this<=max)
        this
    else
        null
}

fun Float?.checkNull(defaultValue: Float = FLOAT_ZERO): Float {
    return this ?: defaultValue
}

/**
 * Check if value is greater or equal than min or return null
 */
fun Float.minOrNull(min: Float): Float? {
    return if(this>=min)
        this
    else
        null
}

/**
 * Check if value is smaller or equal than max or return null
 */
fun Float.maxOrNull(max: Float): Float? {
    return if(this<=max)
        this
    else
        null
}

fun Short?.checkNull(defaultValue: Short = SHORT_ZERO): Short {
    return this ?: defaultValue
}

/**
 * Check if value is greater or equal than min or return null
 */
fun Short.minOrNull(min: Short): Short? {
    return if(this>=min)
        this
    else
        null
}

/**
 * Check if value is smaller or equal than max or return null
 */
fun Short.maxOrNull(max: Short): Short? {
    return if(this<=max)
        this
    else
        null
}

fun Long?.checkNull(defaultValue: Long = LONG_ZERO): Long {
    return this ?: defaultValue
}

/**
 * Check if value is greater or equal than min or return null
 */
fun Long.minOrNull(min: Long): Long? {
    return if(this>=min)
        this
    else
        null
}

/**
 * Check if value is smaller or equal than max or return null
 */
fun Long.maxOrNull(max: Long): Long? {
    return if(this<=max)
        this
    else
        null
}

fun Double?.checkNull(defaultValue: Double = DOUBLE_ZERO): Double {
    return this ?: defaultValue
}

/**
 * Check if value is greater or equal than min or return null
 */
fun Double.minOrNull(min: Double): Double? {
    return if(this>=min)
        this
    else
        null
}

/**
 * Check if value is smaller or equal than max or return null
 */
fun Double.maxOrNull(max: Double): Double? {
    return if(this<=max)
        this
    else
        null
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
