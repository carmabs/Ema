package com.carmabs.ema.android.extension

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.carmabs.ema.core.constants.FLOAT_ONE
import com.carmabs.ema.core.constants.INT_ZERO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
fun @receiver:StringRes Int.getFormattedString(context: Context, vararg data: Any?): String {
    return String.format(context.getString(this), *data)
}

fun @receiver:PluralsRes Int.getFormattedPluralsString(
    context: Context,
    times: Int,
    vararg data: Any?
): String {
    return context.resources.getQuantityString(this, times, *data)
}

fun @receiver:FontRes Int.getTypeface(context: Context): Typeface {
    return ResourcesCompat.getFont(context, this)!!
}


/**
 * Get color from a @ColorRes
 * @param context Application context
 */

fun @receiver:ColorRes Int.getColor(context: Context): Int {
    return ContextCompat.getColor(context, this)
}

fun @receiver:DimenRes Int.getAttributeDimenValue(context: Context): Int {
    val tv = TypedValue()
    return if (context.theme.resolveAttribute(this, tv, true)) {
        TypedValue.complexToDimensionPixelSize(tv.data, context.resources.displayMetrics)
    } else
        INT_ZERO


}

fun @receiver:DrawableRes Int.getDrawable(context: Context): Drawable {
    return ContextCompat.getDrawable(context, this)!!
}

fun @receiver:DimenRes Int.getDimension(context: Context): Float {
    return context.resources.getDimension(this)
}

fun @receiver:DrawableRes Int.getBitmap(
    context: Context,
    width: Int? = null,
    height: Int? = null
): Bitmap {
    return when {
        width != null && height != null -> {
            ContextCompat.getDrawable(context, this)!!.toBitmap(width = width, height = height)
        }
        width != null -> {
            ContextCompat.getDrawable(context, this)!!.toBitmap(width = width)
        }
        height != null -> {
            ContextCompat.getDrawable(context, this)!!.toBitmap(height = height)
        }
        else -> {
            ContextCompat.getDrawable(context, this)!!.toBitmap()
        }

    }
}

fun @receiver:DrawableRes Int.getByteArray(context: Context): ByteArray {
    val drawable = getDrawable(context)
    val bitmap = (drawable as BitmapDrawable).bitmap
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
    return stream.toByteArray()
}

fun @receiver:DrawableRes Int.getBitmapFromResource(
    context: Context,
    width: Int? = null,
    height: Int? = null,
    colorHex: String? = null
): Bitmap {
    return ContextCompat.getDrawable(context, this)?.let {
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

suspend fun @receiver:DrawableRes Int.getBitmapCropFromResource(
    context: Context,
    width: Int? = null,
    height: Int? = null
): Bitmap {

    return withContext(Dispatchers.Default) {
        val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, this@getBitmapCropFromResource)
        val w = width ?: bitmap.width
        val h = height ?: bitmap.height
        bitmap.resizeCrop(w, h)
    }
}
