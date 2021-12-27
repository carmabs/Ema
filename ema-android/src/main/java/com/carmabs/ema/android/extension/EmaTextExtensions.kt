package com.carmabs.ema.android.extension

import android.content.Context
import com.carmabs.ema.core.model.EmaText

/**
 * Created by Carlos Mateo Benito on 25/12/21.
 *
 * <p>
 * Copyright (c) 2021 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */

fun EmaText.string(context: Context):String{
    return when(this){
        is EmaText.Id -> context.getString(id,data)
        is EmaText.Plural -> context.resources.getQuantityString(id,quantity,data)
        is EmaText.Text -> String.format(text,data)
    }
}