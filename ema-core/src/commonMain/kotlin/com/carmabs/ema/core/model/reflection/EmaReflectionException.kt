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
data class EmaReflectionException internal constructor(
    val reflection: EmaReflection,
    val exception:Throwable
)