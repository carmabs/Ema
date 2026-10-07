package com.carmabs.ema.android.extension

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import com.carmabs.ema.core.constants.STRING_EMPTY
import com.carmabs.ema.core.model.EmaText

/*
 * Created by Carlos Mateo Benito on 25/12/21.
 *
 * <p>
 * Copyright (c) 2021 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */

/**
 * Arguments to format the text. The factories of [EmaText] always provide an array, so an empty one means that the
 * text has no arguments and must not be formatted, like a text that contains a % sign.
 */
private val EmaText.formatArgs: Array<out Any>?
    get() = data?.takeIf { it.isNotEmpty() }

/**
 * Transform ema text to string value.
 */
fun EmaText.string(context: Context): String = when (this) {
    is EmaText.Id -> formatArgs?.let { context.getString(id, *it) } ?: context.getString(id)

    is EmaText.Plural -> {
        formatArgs?.let {
            context.resources.getQuantityString(id, quantity, *it)
        } ?: context.resources.getQuantityString(
            id,
            quantity
        )
    }

    is EmaText.Text -> formatArgs?.let { String.format(text, *it) } ?: text

    is EmaText.Composition -> {
        texts.fold(STRING_EMPTY) { acc, emaText ->
            acc + emaText.string(context)
        }
    }
}
fun @receiver:StringRes Int.toEmaText(vararg data: Any): EmaText = EmaText.id(id = this, data = data)
fun @receiver:PluralsRes Int.toEmaText(quantity: Int, vararg data: Any): EmaText =
    EmaText.plural(id = this, quantity = quantity, data = data)
