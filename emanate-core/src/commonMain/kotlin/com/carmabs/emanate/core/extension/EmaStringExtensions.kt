package com.carmabs.emanate.core.extension

import com.carmabs.emanate.core.constants.STRING_EMPTY

/*
 * Created by Carlos Mateo Benito on 2019-11-24.
 *
 * <p>
 * Copyright (c) 2019 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */

/**
 * Returns the string, or [defaultValue] if it is null.
 */
fun String?.toNonNull(defaultValue: String = STRING_EMPTY): String = this ?: defaultValue

/**
 * Returns the string, or [defaultValue] if it is null or empty.
 */
fun String?.toNonEmpty(defaultValue: String): String = if (isNullOrEmpty()) defaultValue else this

/**
 * Replaces the last [delimiter] with [newString]. The text on each side of the delimiter is trimmed. If there is no
 * [delimiter], the string is returned as it is.
 */
fun String.replaceLast(delimiter: Char, newString: String): String {
    val index = lastIndexOf(delimiter)
    if (index < 0) return this
    return substring(0, index).trim() + newString + substring(index + 1).trim()
}
