package com.carmabs.ema.android.extension

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.core.content.ContextCompat
import com.carmabs.ema.core.constants.INT_ZERO


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
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
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

    val w = width?:bitmap.width
    val h = height?:bitmap.height

    val sourceWidth: Int = bitmap.width
    val sourceHeight: Int = bitmap.height

    // Compute the scaling factors to fit the new height and width, respectively.
    // To cover the final image, the final scaling will be the bigger
    // of these two.

    // Compute the scaling factors to fit the new height and width, respectively.
    // To cover the final image, the final scaling will be the bigger
    // of these two.
    val xScale = w  / sourceWidth.toFloat()
    val yScale = w  / sourceHeight.toFloat()
    val scale = xScale.coerceAtLeast(yScale)

    // Now get the size of the source bitmap when scaled

    // Now get the size of the source bitmap when scaled
    val scaledWidth = scale * sourceWidth
    val scaledHeight = scale * sourceHeight

    // Let's find out the upper left coordinates if the scaled bitmap
    // should be centered in the new size give by the parameters

    // Let's find out the upper left coordinates if the scaled bitmap
    // should be centered in the new size give by the parameters
    val left: Float = (w - scaledWidth) / 2
    val top: Float = (h - scaledHeight) / 2

    // The target rectangle for the new, scaled version of the source bitmap will now
    // be

    // The target rectangle for the new, scaled version of the source bitmap will now
    // be
    val targetRect = RectF(left, top, left + scaledWidth, top + scaledHeight)

    // Finally, we create a new bitmap of the specified size and draw our new,
    // scaled bitmap onto it.

    // Finally, we create a new bitmap of the specified size and draw our new,
    // scaled bitmap onto it.
    val dest = Bitmap.createBitmap(w, h, bitmap.getConfig())
    val canvas = Canvas(dest)
    canvas.drawBitmap(bitmap, null, targetRect, null)

    return dest
}
