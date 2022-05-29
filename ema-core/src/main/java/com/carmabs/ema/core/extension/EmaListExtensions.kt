package com.carmabs.ema.core.extension

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