package com.carmabs.emanate.android.utils

import android.content.Context
import android.graphics.Insets
import android.os.Build
import android.util.DisplayMetrics
import android.util.Size
import android.view.WindowInsets
import android.view.WindowManager
import com.carmabs.emanate.core.utils.EmaScreenUtils
import com.carmabs.emanate.core.utils.EmaUtils

/**
 * Screen utilities of Android. Use them through `EmaUtils.Screen.Android`.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
object EmaAndroidScreenUtils {

    /**
     * Returns the metrics of the screen. The size excludes the navigation bars and the display cutout, unless
     * [includeInsets] is true.
     */
    fun metrics(context: Context, includeInsets: Boolean = false): DisplayMetrics {
        val displayMetrics = context.resources.displayMetrics
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager.currentWindowMetrics
            // Gets all excluding insets
            val windowInsets = metrics.windowInsets
            val insets: Insets = windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.navigationBars()
                    or WindowInsets.Type.displayCutout()
            )

            val insetsWidth: Int = insets.right + insets.left
            val insetsHeight: Int = insets.top + insets.bottom

            // Legacy size that Display#getSize reports
            val bounds = metrics.bounds
            val legacySize = Size(
                bounds.width() - insetsWidth,
                bounds.height() - insetsHeight
            )
            displayMetrics.apply {
                heightPixels = legacySize.height
                widthPixels = legacySize.width
            }
            // Get the application size including decoration
            if (includeInsets) {
                val boundsActivity = windowManager.currentWindowMetrics.bounds
                displayMetrics.apply {
                    heightPixels = boundsActivity.height()
                    widthPixels = boundsActivity.width()
                }
            }
        } else {
            windowManager.defaultDisplay.getMetrics(displayMetrics)
        }
        return displayMetrics
    }
}

/**
 * Screen utilities of Android, for example `EmaUtils.Screen.Android.metrics(context)`.
 */
val EmaScreenUtils.Android: EmaAndroidScreenUtils
    get() = EmaAndroidScreenUtils
