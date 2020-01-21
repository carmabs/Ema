package com.carmabs.ema.core.extension

import com.carmabs.ema.core.constants.*

/**
 * Created by Carlos Mateo Benito on 2019-11-24.
 *
 * <p>
 * Copyright (c) 2019 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */

fun String?.checkNull(defaultValue: String = STRING_EMPTY): String {
    return this ?: defaultValue
}

fun String.getFormattedString(vararg data: Any?): String {
    return String.format(this, *data)
}
