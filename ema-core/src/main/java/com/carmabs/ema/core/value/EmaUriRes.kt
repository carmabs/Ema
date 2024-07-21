package com.carmabs.ema.core.value

/**
 * Created by Carlos Mateo Benito on 2/2/24.
 *
 * <p>
 * Copyright (c) 2024 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */

class EmaUriRes(val value: String, val type:EmaUriType)

enum class EmaUriType{
    Color,
    Drawable,
    String,
    Plural,
    Asset,
    Raw
}
fun String.toUriRes(type:EmaUriType) = EmaUriRes(this,type)
