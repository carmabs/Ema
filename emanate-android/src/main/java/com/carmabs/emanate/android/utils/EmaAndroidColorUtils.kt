package com.carmabs.emanate.android.utils

import android.graphics.Color
import androidx.annotation.ColorInt
import com.carmabs.emanate.core.utils.EmaColorUtils
import com.carmabs.emanate.core.utils.EmaUtils
import kotlin.math.floor

/**
 * Color utilities of Android. Use them through `EmaUtils.Color.Android`.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
object EmaAndroidColorUtils {

    /**
     * Returns the color at [value] of a gradient of [colors], each one at the position of the same index in
     * [positions], interpolating between the two closest ones. A value out of the positions returns the color of the
     * closest end.
     */
    @ColorInt
    fun fromGradient(@ColorInt colors: IntArray, positions: FloatArray, value: Float): Int {
        require(colors.isNotEmpty() && colors.size == positions.size) {
            "There must be one position for each color"
        }
        if (colors.size == 1 || value <= positions.first()) return colors.first()
        if (value >= positions.last()) return colors.last()
        val index = positions.indexOfFirst { value <= it }
        val t = (value - positions[index - 1]) / (positions[index] - positions[index - 1])
        return lerp(colors[index - 1], colors[index], t)
    }

    /**
     * Interpolates each channel, alpha included, from [colorA] to [colorB]: [t] 0 returns [colorA] and 1, [colorB].
     */
    @ColorInt
    fun lerp(@ColorInt colorA: Int, @ColorInt colorB: Int, t: Float): Int {
        fun channel(a: Int, b: Int): Int = floor(a * (1 - t) + b * t).toInt()
        return Color.argb(
            channel(Color.alpha(colorA), Color.alpha(colorB)),
            channel(Color.red(colorA), Color.red(colorB)),
            channel(Color.green(colorA), Color.green(colorB)),
            channel(Color.blue(colorA), Color.blue(colorB))
        )
    }
}

/**
 * Color utilities of Android, for example `EmaUtils.Color.Android.lerp(colorA, colorB, 0.5f)`.
 */
val EmaColorUtils.Android: EmaAndroidColorUtils
    get() = EmaAndroidColorUtils
