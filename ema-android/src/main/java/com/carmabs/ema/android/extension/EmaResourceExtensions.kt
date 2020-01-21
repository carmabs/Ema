package com.carmabs.ema.android.extension

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

/**
 *  *<p>
 * Copyright (c) 2020, Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 *
 * Created by: Carlos Mateo Benito on 20/1/19.
 */

/**
 * Get a string through a @StringRes formatted with  params
 * @param context Application context
 * @param data Params to be set on the string.
 *
 * Example: Hello world %d -> data = 1 -> Hello wworld 1
 */
fun Int.getFormattedString(context: Context, vararg data: Any?): String {
    return String.format(context.getString(this), *data)
}

/**
 * Get color from a @ColorRes
 * @param context Application context
 */
@RequiresApi(Build.VERSION_CODES.M)
fun Int.getColor(context: Context): Int {
    return context.resources.getColor(this,context.theme)
}

/**
 * Get color from a @ColorRes from api lower than 23
 * @param context Application context
 */
fun Int.getColorCompat(context: Context):Int {
    return ContextCompat.getColor(context,this)
}

