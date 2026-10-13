package com.carmabs.emanate.core.utils

import com.carmabs.emanate.core.constants.INT_ONE

/**
 * Size utilities. Use them through `EmaUtils.Size`.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
object EmaSizeUtils {

    /**
     * Scales a size of [currentWidth] x [currentHeight] keeping its proportions, so its longest side matches the target:
     * [targetHeight] for a portrait size, [targetWidth] otherwise.
     * @return the new width and height
     */
    fun fitInTargetMaxSizeProportionally(
        currentWidth: Int,
        currentHeight: Int,
        targetWidth: Int,
        targetHeight: Int
    ): Pair<Int, Int> {
        val widthHeightRatio = currentWidth / currentHeight.toFloat()
        val endWidth = if (widthHeightRatio < INT_ONE) targetHeight * widthHeightRatio else targetWidth.toFloat()
        val endHeight = if (widthHeightRatio < INT_ONE) targetHeight.toFloat() else targetWidth / widthHeightRatio
        return Pair(endWidth.toInt(), endHeight.toInt())
    }
}
