package com.carmabs.emanate.android.extension

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Insets
import android.os.Build
import android.util.DisplayMetrics
import android.util.Size
import android.util.TypedValue
import android.view.WindowInsets
import android.view.WindowManager
import com.carmabs.emanate.core.constants.INT_ONE
import com.carmabs.emanate.core.constants.INT_ZERO
import kotlin.math.roundToInt

/*
 * Extension methods for display feature
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

/**
 * Return the Int dp into its equivalent px
 */
val Int.dp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).roundToInt()

/**
 * Return the Int dp into its equivalent px
 */
val Float.dp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )

/**
 * Return the Int sp into its equivalent px
 */
val Int.sp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).roundToInt()

/**
 * Return the Float sp into its equivalent px
 */
val Float.sp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        Resources.getSystem().displayMetrics
    )

fun Context.getStatusBarHeight(): Int {
    val resourceId: Int =
        resources.getIdentifier("status_bar_height", "dimen", "android")

    return if (resourceId > 0) {
        resources.getDimensionPixelSize(resourceId)
    } else {
        INT_ZERO
    }
}

val Configuration.screenHeightPx
    get() = run { screenHeightDp.toFloat() * densityDpi / 160f }

val Configuration.screenWidthPx
    get() = run { screenWidthDp.toFloat() * densityDpi / 160f }
