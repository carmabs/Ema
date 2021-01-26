package com.carmabs.ema.android.extension

import android.graphics.*

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
): Bitmap? {
    val output: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val color = -0xbdbdbe
    val pColor = paint.color
    val pAlias = paint.isAntiAlias
    val pXferMode = paint.xfermode

    rect.set(0,0,width,height)
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