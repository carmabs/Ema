package com.carmabs.ema.android.extension

import android.content.Context
import com.carmabs.ema.core.constants.INT_ZERO
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

/**
 * Transform ema text to string value.
 * @param overrideParams can use these params to override the extra params set in the EmaText object originally
 */
fun EmaText.string(context: Context,vararg overrideParams:Any):String{
    val areEmptyParams = overrideParams.isNullOrEmpty()
    val params = if(areEmptyParams)
        data
    else
        overrideParams
    return when(this){
        is EmaText.Id -> { context.getString(id, params) }
        is EmaText.Plural -> {
            val (updatedQuantity,updatedData) = if(areEmptyParams)
                Pair(quantity,data)
            else
                Pair(overrideParams.first() as Int,overrideParams.drop(INT_ZERO).toTypedArray())
            context.resources.getQuantityString(id,updatedQuantity,*updatedData)
        }
        is EmaText.Text -> String.format(text,params)
    }
}