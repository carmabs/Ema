package com.carmabs.ema.android.extension

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager
import com.carmabs.ema.core.constants.INT_ZERO
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

/**
 * Get display metrics
 */
fun getScreenMetrics(context: Context): DisplayMetrics {
    val displayMetrics = context.resources.displayMetrics
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val boundsActivity = windowManager.currentWindowMetrics.bounds
        displayMetrics.apply {
            heightPixels = boundsActivity.height()
            widthPixels = boundsActivity.width()
        }

    }
    else {
        windowManager.defaultDisplay.getMetrics(displayMetrics)
    }
    return displayMetrics
}

fun Context.getStatusBarHeight():Int{
    val resourceId: Int =
        resources.getIdentifier("status_bar_height", "dimen", "android")

    return if (resourceId > 0) {
        resources.getDimensionPixelSize(resourceId)
    } else
        INT_ZERO
}