package com.carmabs.ema.android.extension

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable


/**
 * Extensions for bundles
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

/**
 * Add parcelable method by reified template to allow new getParcelable implementation for api > TIRAMISU (33)
 */
inline fun <reified T : Parcelable> Bundle.getParcelableCompat(name: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelable(name, T::class.java)
    } else {
        getParcelable(name) as? T
    }
}

/**
 * Add serializable method by reified template to allow new getSerializable implementation for api > TIRAMISU (33)
 */
inline fun <reified T : java.io.Serializable> Bundle.getSerializableCompat(name: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializable(name, T::class.java)
    } else {
        getSerializable(name) as? T
    }
}
