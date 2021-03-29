package com.carmabs.ema.core.viewmodel

/**
 * Model to handle activity result feature
 *
 * @id Id for the code for activity result
 * @implementation Function to handle activity result. Return true to remove the listener after use it
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
data class EmaResultModel(
    val code: Int,
    val data: Any,
    val ownerId:Int
)