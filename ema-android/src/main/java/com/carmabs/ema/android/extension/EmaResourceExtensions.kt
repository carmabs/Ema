package com.carmabs.ema.android.extension

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import java.lang.Exception


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

fun Int.getColor(context: Context): Int {
    return ContextCompat.getColor(context,this)
}

fun Int.getDrawable(context: Context): Drawable? {
    return ContextCompat.getDrawable(context,this)
}

fun getBitmapFromResource(context: Context, drawableId: Int, width:Int?=null, height:Int?=null): Bitmap {
    return ContextCompat.getDrawable(context, drawableId)?.let {
        val drawable: Drawable =it
        val bitmap: Bitmap = Bitmap.createBitmap(
            width?:drawable.intrinsicWidth,
            height?:drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    }?:throw Exception("Drawable not found")
}
