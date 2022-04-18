package com.carmabs.ema.android.extension

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.ColorInt
import com.carmabs.ema.core.constants.INT_ZERO
import com.carmabs.ema.core.model.EmaImage
import com.carmabs.ema.core.model.EmaText

/**
 * Created by Carlos Mateo Benito on 17/04/21.
 *
 * <p>
 * Copyright (c) 2021 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */

/**
 * Transform ema image to bitmap value.
 * @param overrideWeight to override EmaImage setted weight
 * @param overrideHeight to override EmaImage setted height
 * @param overrideTint to override EmaImage setted tint
 */
suspend fun EmaImage.bitmap(context: Context, overrideWeight:Int?=null,overrideHeight:Int? = null,@ColorInt overrideTint:Int?=null):Bitmap{
    val fWidth = overrideWeight?:width
    val fHeight = overrideHeight?:height
    val fTint = overrideTint?:colorTint
    return when(this){
        is EmaImage.ByteArray -> bytes.toBitmap(fWidth,fHeight,fTint)
        is EmaImage.Id -> id.getBitmapFromResource(context,fWidth,fHeight,fTint)
    }
}