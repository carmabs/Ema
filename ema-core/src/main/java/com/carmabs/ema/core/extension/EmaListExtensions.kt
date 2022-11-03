package com.carmabs.ema.core.extension

import com.carmabs.ema.core.constants.INT_ONE
import com.carmabs.ema.core.constants.INT_ZERO

/**
 * Created by Carlos Mateo Benito on 2022-05-29.
 *
 * <p>
 * Copyright (c) 2022 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */

/**
 * Update the item with the new one provided.
 */
fun <T> List<T>.update(findCriteria: ((T) -> Boolean), updateAction: (T.() -> T)): List<T> {
    val itemIndex = indexOfFirst {
        findCriteria(it)
    }

    return if (itemIndex < INT_ZERO) {
        toList()
    } else {
        val itemToUpdate = get(itemIndex)
        val newItem = updateAction.invoke(itemToUpdate)
        val updatedList = toMutableList()
        updatedList[itemIndex] = newItem
        updatedList
    }
}

fun <T> List<T>.forEachFirstLast(action: (T) -> Unit) {
    firstOrNull()?.let {
        action.invoke(it)
        drop(INT_ONE).internalForEachFirstLast(false, action)
    }
}

internal fun <T> List<T>.internalForEachFirstLast(first: Boolean, action: (T) -> Unit) {
    if (first) {
        firstOrNull()?.also {
            drop(INT_ONE).internalForEachFirstLast(false, action)
        }
    } else {
        lastOrNull()?.also {
            dropLast(INT_ONE).internalForEachFirstLast(true, action)
        }
    }
}

fun <T, R> List<T>.mapFirstLast(
    action: (T) -> R
): List<R> {
    return firstOrNull()?.let {
        drop(INT_ONE).internalMapFirstLast(transformList = mutableListOf(action(it)), first = false, action = action)
    } ?: emptyList()
}

private fun <T, R> List<T>.internalMapFirstLast(
    transformList: MutableList<R>,
    first: Boolean,
    action: (T) -> R
): List<R> {
    if (first) {
        firstOrNull()?.also {
            transformList.add(action.invoke(it))
            drop(INT_ONE).internalMapFirstLast(transformList, false, action)
        }
    } else {
        lastOrNull()?.also {
            transformList.add(action.invoke(it))
            dropLast(INT_ONE).internalMapFirstLast(transformList, true, action)
        }
    }

    return transformList
}