package com.carmabs.ema.android.extension

import android.view.View
import android.view.ViewTreeObserver


/**
 * Extensions for views
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

/**
 * Listener to make view tasks after it has been measured
 */
inline fun View.afterMeasured(crossinline f: View.() -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                f()
            }
        }
    })
}

/**
 * Returns View visibility based on boolean
 * @param visibility True is VISIBLE, false INVISIBLE
 * @param gone Put this value true if visibility is false and you want to return GONE
 *
 */
fun checkVisibility(visibility: Boolean, gone: Boolean = true): Int {
    return when {
        visibility -> View.VISIBLE
        !visibility && gone -> View.GONE
        else -> View.INVISIBLE
    }
}

/**
 * Executes an action if values are different
 *
 * @param oldValue Previous value to check update
 * @param newValue Current value to check update
 * @param action Function to execute with new value if it has changed
 */
fun <T> checkUpdate(oldValue: T, newValue: T, action: (T) -> Unit) {
    if (oldValue != newValue)
        action(newValue)
}