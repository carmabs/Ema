package com.carmabs.ema.android.extension

/**
 * String extensions that rely on Java APIs, so they are available only on Android/JVM.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */

fun String.getFormattedString(vararg data: Any?): String = String.format(this, *data)
