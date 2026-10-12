package com.carmabs.emanate.core.extension

import com.carmabs.emanate.core.constants.DOUBLE_ZERO
import com.carmabs.emanate.core.constants.FLOAT_ZERO
import com.carmabs.emanate.core.constants.INT_ONE
import com.carmabs.emanate.core.constants.INT_ZERO
import com.carmabs.emanate.core.constants.LONG_ZERO
import com.carmabs.emanate.core.constants.SHORT_ZERO
import kotlin.math.ceil

/*
 * Created by Carlos Mateo Benito on 2019-11-24.
 *
 * <p>
 * Copyright (c) 2019 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */

/**
 * Returns the value, or [defaultValue] if it is null.
 */
fun Int?.toNonNull(defaultValue: Int = INT_ZERO): Int = this ?: defaultValue

/**
 * Returns the value if it is greater than or equal to [min], or null otherwise.
 */
fun Int.minOrNull(min: Int): Int? = if (this >= min) {
    this
} else {
    null
}

/**
 * Returns the value if it is less than or equal to [max], or null otherwise.
 */
fun Int.maxOrNull(max: Int): Int? = if (this <= max) {
    this
} else {
    null
}

/**
 * Returns the value, or [defaultValue] if it is null.
 */
fun Float?.toNonNull(defaultValue: Float = FLOAT_ZERO): Float = this ?: defaultValue

/**
 * Returns the value if it is greater than or equal to [min], or null otherwise.
 */
fun Float.minOrNull(min: Float): Float? = if (this >= min) {
    this
} else {
    null
}

/**
 * Returns the value if it is less than or equal to [max], or null otherwise.
 */
fun Float.maxOrNull(max: Float): Float? = if (this <= max) {
    this
} else {
    null
}

/**
 * Returns true if the value has a fractional part.
 */
fun Float.hasDecimals(): Boolean = this != ceil(this)

/**
 * Returns the value, or [defaultValue] if it is null.
 */
fun Short?.toNonNull(defaultValue: Short = SHORT_ZERO): Short = this ?: defaultValue

/**
 * Returns the value if it is greater than or equal to [min], or null otherwise.
 */
fun Short.minOrNull(min: Short): Short? = if (this >= min) {
    this
} else {
    null
}

/**
 * Returns the value if it is less than or equal to [max], or null otherwise.
 */
fun Short.maxOrNull(max: Short): Short? = if (this <= max) {
    this
} else {
    null
}

/**
 * Returns the value, or [defaultValue] if it is null.
 */
fun Long?.toNonNull(defaultValue: Long = LONG_ZERO): Long = this ?: defaultValue

/**
 * Returns the value if it is greater than or equal to [min], or null otherwise.
 */
fun Long.minOrNull(min: Long): Long? = if (this >= min) {
    this
} else {
    null
}

/**
 * Returns the value if it is less than or equal to [max], or null otherwise.
 */
fun Long.maxOrNull(max: Long): Long? = if (this <= max) {
    this
} else {
    null
}

/**
 * Returns the value, or [defaultValue] if it is null.
 */
fun Double?.toNonNull(defaultValue: Double = DOUBLE_ZERO): Double = this ?: defaultValue

/**
 * Returns the value if it is greater than or equal to [min], or null otherwise.
 */
fun Double.minOrNull(min: Double): Double? = if (this >= min) {
    this
} else {
    null
}

/**
 * Returns the value if it is less than or equal to [max], or null otherwise.
 */
fun Double.maxOrNull(max: Double): Double? = if (this <= max) {
    this
} else {
    null
}

/**
 * Returns the value, or [defaultValue] if it is null.
 */
fun Boolean?.toNonNull(defaultValue: Boolean = false): Boolean = this ?: defaultValue

/**
 * Limits the value to the range from [minLimit] to [maxLimit].
 */
fun Float.constraintValue(minLimit: Float, maxLimit: Float): Float = coerceAtLeast(minLimit).coerceAtMost(maxLimit)

/**
 * Limits the value to the range from [minLimit] to [maxLimit].
 */
fun Int.constraintValue(minLimit: Int, maxLimit: Int): Int = coerceAtLeast(minLimit).coerceAtMost(maxLimit)

/**
 * Returns the position in a list of [length] items of the step [index] of a walk that goes from the center to the ends,
 * alternating sides: center, center - 1, center + 1, center - 2... The sides are swapped when [startRight] is false.
 * An [index] out of the list returns the center.
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
            } else {
                if (startRight) -INT_ONE else INT_ONE
            }

            val coefficient = ((index + INT_ONE) / 2) * directionFactor
            centerPosition + coefficient
        }
    }
    return position
}

/**
 * Returns the position in a list of [length] items of the step [index] of a walk that goes from the ends to the center,
 * alternating sides: last, first, second to last, second... The sides are swapped when [startRight] is false. An
 * [index] out of the list returns the center.
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
            val right = if ((index + INT_ONE) % 2 != INT_ZERO) {
                startRight
            } else {
                !startRight
            }

            val coefficient = (index / 2)

            if (right) {
                length - coefficient - INT_ONE
            } else {
                coefficient
            }
        }
    }
    return position
}
