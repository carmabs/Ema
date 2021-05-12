package com.carmabs.ema.android.extension

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.core.content.ContextCompat
import com.carmabs.ema.core.constants.FLOAT_ONE
import com.carmabs.ema.core.constants.INT_ZERO
import java.io.ByteArrayOutputStream


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
    return ContextCompat.getColor(context, this)
}

fun Int.getAttributeDimenValue(context: Context): Int {
    val tv = TypedValue()
    return if (context.theme.resolveAttribute(this, tv, true)) {
        TypedValue.complexToDimensionPixelSize(tv.data, context.resources.displayMetrics)
    } else
        INT_ZERO


}

fun @receiver:androidx.annotation.DrawableRes Int.getDrawable(context: Context): Drawable {
    return ContextCompat.getDrawable(context, this)!!
}

fun @receiver:androidx.annotation.DrawableRes Int.getByteArray(context: Context): ByteArray {
    val drawable = getDrawable(context)
    val bitmap = (drawable as BitmapDrawable).bitmap
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
    return  stream.toByteArray()
}

fun getBitmapFromResource(
    context: Context,
    drawableId: Int,
    width: Int? = null,
    height: Int? = null,
    colorHex: String? = null
): Bitmap {
    return ContextCompat.getDrawable(context, drawableId)?.let {
        val drawable: Drawable = it
        val bitmap: Bitmap = Bitmap.createBitmap(
            width ?: drawable.intrinsicWidth,
            height ?: drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        // TO ANTIALIAS //
        val ratio = drawable.intrinsicWidth / drawable.intrinsicHeight.toFloat()
        val boundWidth = when {
            ratio < FLOAT_ONE -> bitmap.height * ratio
            else -> bitmap.width.toFloat()
        }
        val boundHeight = when {
            ratio < FLOAT_ONE -> bitmap.height.toFloat()
            else -> bitmap.width / ratio
        }
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        drawable.setBounds(
            (bitmap.width / 2f - boundWidth / 2f).toInt(),
            (bitmap.height / 2f - boundHeight / 2f).toInt(),
            (bitmap.width / 2f + boundWidth / 2f).toInt(),
            (bitmap.height / 2f + boundHeight / 2f).toInt()
        )
        colorHex?.also { color -> drawable.setTint(Color.parseColor(color)) }
        drawable.draw(canvas)
        bitmap
    } ?: throw Exception("Drawable not found")
}

fun getBitmapCropFromResource(
    context: Context,
    drawableId: Int,
    width: Int? = null,
    height: Int? = null
): Bitmap {

    val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, drawableId)

    val w = width ?: bitmap.width
    val h = height ?: bitmap.height

    return bitmap.resize(w,h)
}
