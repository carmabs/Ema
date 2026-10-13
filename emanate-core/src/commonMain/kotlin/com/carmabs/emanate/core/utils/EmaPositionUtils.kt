package com.carmabs.emanate.core.utils

import com.carmabs.emanate.core.constants.INT_ONE
import com.carmabs.emanate.core.constants.INT_ZERO

/**
 * Utilities to walk the positions of a list in a different order. Use them through `EmaUtils.Positions`.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
object EmaPositionUtils {

    /**
     * Returns the position in a list of [length] items of the step [index] of a walk that goes from the center to the
     * ends, alternating sides: center, center - 1, center + 1, center - 2... The sides are swapped when [startRight] is
     * false. An [index] out of the list returns the center.
     */
    fun iterateFromCenter(index: Int, length: Int, startRight: Boolean = true): Int {
        val centerPosition = length / 2
        if (index <= INT_ZERO || index >= length) return centerPosition
        val directionFactor = if (index % 2 == INT_ZERO) {
            if (startRight) INT_ONE else -INT_ONE
        } else {
            if (startRight) -INT_ONE else INT_ONE
        }
        val coefficient = ((index + INT_ONE) / 2) * directionFactor
        return centerPosition + coefficient
    }

    /**
     * Returns the position in a list of [length] items of the step [index] of a walk that goes from the ends to the
     * center, alternating sides: last, first, second to last, second... The sides are swapped when [startRight] is
     * false. An [index] out of the list returns the center.
     */
    fun iterateFromEnd(index: Int, length: Int, startRight: Boolean = true): Int {
        if (index < INT_ZERO || index >= length) return length / 2
        val right = if ((index + INT_ONE) % 2 != INT_ZERO) startRight else !startRight
        val coefficient = index / 2
        return if (right) length - coefficient - INT_ONE else coefficient
    }
}
