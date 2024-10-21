package com.carmabs.ema.core.model.reflection

/**
 * Created by Carlos Mateo Benito on 20/10/24.
 *
 * <p>
 * Copyright (c) 2024 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
data class EmaReflection internal constructor(
    val containerClassName:String,
    val containerClassQualifiedName:String,
    val methodName:String
)