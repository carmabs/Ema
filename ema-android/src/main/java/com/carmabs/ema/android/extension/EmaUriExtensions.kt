package com.carmabs.ema.android.extension

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.carmabs.ema.core.value.EmaUriRes
import com.carmabs.ema.core.value.EmaUriType
import java.io.InputStream

/**
 * Created by Carlos Mateo Benito on 2/2/24.
 *
 * <p>
 * Copyright (c) 2024 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
@SuppressLint("DiscouragedApi")
fun EmaUriRes.getResourceId(context: Context, type: String = "id"): Int {
    return context.resources.getIdentifier(
        Uri.parse(value).lastPathSegment,
        type,
        context.packageName
    )
}

inline fun EmaUriRes.onDrawable(context: Context, onAction: Drawable.() -> Unit): EmaUriRes {
    if (type == EmaUriType.Drawable) {
        requireDrawable(context).onAction()
    }
    return this
}

inline fun EmaUriRes.onBitmap(
    context: Context,
    width: Int? = null,
    height: Int? = null,
    onAction: Bitmap.() -> Unit
): EmaUriRes {
    onDrawable(context) {
        val bitmap = requireBitmap(context, width, height)
        bitmap.onAction()
    }

    return this

}

inline fun EmaUriRes.onColor(context: Context, action: Int.() -> Unit): EmaUriRes {
    if (type == EmaUriType.Color) {
        ContextCompat.getColor(context, getResourceId(context)).action()
    }
    return this
}

inline fun EmaUriRes.onString(
    context: Context,
    vararg args: Any?,
    action: String.() -> Unit
): EmaUriRes {
    if (type == EmaUriType.String) {
        getResourceId(context).getFormattedString(context, args).action()
    }
    return this
}

inline fun EmaUriRes.onPlural(
    context: Context,
    times: Int,
    vararg args: Any?,
    action: String.() -> Unit
): EmaUriRes {
    if (type == EmaUriType.Plural) {
        getResourceId(context).getFormattedPluralsString(context, times, args).action()
    }
    return this
}

inline fun EmaUriRes.onAsset(context: Context, action: InputStream.() -> Unit): EmaUriRes {
    if (type == EmaUriType.Asset) {
        context.assets.open(value).action()
    }
    return this
}

inline fun EmaUriRes.onRaw(context: Context, action: InputStream.() -> Unit): EmaUriRes {
    if (type == EmaUriType.Raw) {
        getResourceId(context).let {
            context.resources.openRawResource(it)
        }.action()
    }
    return this
}

fun EmaUriRes.requireDrawable(context: Context): Drawable {
    val inputStream = context.contentResolver.openInputStream(Uri.parse(value))
    val drawable = Drawable.createFromStream(inputStream, value)

    val resourceId = getResourceId(context, "drawable")
    val vectorDrawable =
        VectorDrawableCompat.create(context.resources, resourceId, context.theme)?.current

    return (drawable ?: vectorDrawable!!)
}

fun EmaUriRes.requireBitmap(
    context: Context,
    width: Int? = null,
    height: Int? = null
): Bitmap {
    val drawable = requireDrawable(context)
    val dimensions = drawable.fitInTargetMaxSizeProportionally(width, height)
    return drawable.toBitmap(dimensions.first, dimensions.second)
}