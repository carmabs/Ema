package com.carmabs.ema.android.extension

import android.content.Context
import android.util.TypedValue
import kotlin.math.roundToInt

/**
 * Extension methods for display feature
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

/**
 * Convert a dp integer to pixel
 */
fun Int.dpToPx(context: Context):Int {
    return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, toFloat(), context.resources.displayMetrics).roundToInt()
}