package com.carmabs.ema.android.extension

import android.content.Intent
import android.os.Build
import android.os.Parcelable


/**
 * Extensions for intents
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

/**
 * Add parcelable method by reified template to allow new getParcelableExtra implementation for api > TIRAMISU (33)
 */
inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(name: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(name, T::class.java)
    } else {
        getParcelableExtra(name)
    }
}

/**
 * Add serializable method by reified template to allow new getSerializableExtra implementation for api > TIRAMISU (33)
 */
inline fun <reified T : java.io.Serializable> Intent.getSerializableExtraCompat(name: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializableExtra(name, T::class.java)
    } else {
        getSerializableExtra(name) as? T
    }
}
