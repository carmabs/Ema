package com.carmabs.ema.android.extension

import android.graphics.*
import java.io.ByteArrayOutputStream

/**
 * Created by Carlos Mateo Benito on 2020-07-13.
 *
 * <p>
 * Copyright (c) 2020 by atSistemas. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:cmateo.benito@atsistemas.com”>Carlos Mateo Benito</a>
 */
fun Bitmap.getRoundedCornerBitmap(
    pixels: Int,
    paint: Paint = Paint(),
    rect: Rect = Rect(),
    rectF: RectF = RectF()
): Bitmap {
    val output: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val color = -0xbdbdbe
    val pColor = paint.color
    val pAlias = paint.isAntiAlias
    val pXferMode = paint.xfermode

    rect.set(0, 0, width, height)
    rectF.set(rect)
    val roundPx = pixels.toFloat()
    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color
    canvas.drawRoundRect(rectF, roundPx, roundPx, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(this, rect, rect, paint)

    paint.xfermode = pXferMode
    paint.color = pColor
    paint.isAntiAlias = pAlias
    return output
}

fun Bitmap.toByteArray(): ByteArray {
    val stream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 100, stream)
    return stream.toByteArray()
}

fun ByteArray.toBitmap(): Bitmap {
    return BitmapFactory.decodeByteArray(this, 0, size);
}

fun Bitmap.resize(width:Int,height:Int): Bitmap {

    val sourceWidth: Int = this.width
    val sourceHeight: Int = this.height

    // Compute the scaling factors to fit the new height and width, respectively.
    // To cover the final image, the final scaling will be the bigger
    // of these two.

    // Compute the scaling factors to fit the new height and width, respectively.
    // To cover the final image, the final scaling will be the bigger
    // of these two.
    val xScale = width / sourceWidth.toFloat()
    val yScale = width / sourceHeight.toFloat()
    val scale = xScale.coerceAtLeast(yScale)

    // Now get the size of the source bitmap when scaled

    // Now get the size of the source bitmap when scaled
    val scaledWidth = scale * sourceWidth
    val scaledHeight = scale * sourceHeight

    // Let's find out the upper left coordinates if the scaled bitmap
    // should be centered in the new size give by the parameters

    // Let's find out the upper left coordinates if the scaled bitmap
    // should be centered in the new size give by the parameters
    val left: Float = (width - scaledWidth) / 2
    val top: Float = (height - scaledHeight) / 2

    // The target rectangle for the new, scaled version of the source bitmap will now
    // be

    // The target rectangle for the new, scaled version of the source bitmap will now
    // be
    val targetRect = RectF(left, top, left + scaledWidth, top + scaledHeight)

    // Finally, we create a new bitmap of the specified size and draw our new,
    // scaled bitmap onto it.

    // Finally, we create a new bitmap of the specified size and draw our new,
    // scaled bitmap onto it.
    val dest = Bitmap.createBitmap(width, height, this.config)
    val canvas = Canvas(dest)
    canvas.drawBitmap(this, null, targetRect, null)

    return dest
}