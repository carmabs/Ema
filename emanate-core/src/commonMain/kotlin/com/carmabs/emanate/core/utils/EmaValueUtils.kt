package com.carmabs.emanate.core.utils

/**
 * Utilities to react to the changes of a value. Use them through `EmaUtils.Values`.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
object EmaValueUtils {

    /**
     * Runs [action] with [newValue] only if it is different from [oldValue].
     */
    fun <T> checkUpdate(oldValue: T, newValue: T, action: (T) -> Unit) {
        if (oldValue != newValue) {
            action(newValue)
        }
    }
}
