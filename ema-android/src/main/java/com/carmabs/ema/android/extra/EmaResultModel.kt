package com.carmabs.ema.android.extra

import java.io.Serializable

/**
 * Model to handle activity result feature
 *
 * @id Id for the code for activity result
 * @implementation Function to handle activity result. Return true to remove the listener after use it
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
data class EmaResultModel(
        internal val id: Int,
        val data:Serializable,
        val resultState : Result = Result.Success) {

    sealed class Result(val code: Int) {

        object Success : Result(-1)

        object Fail : Result(0)

        class Other(code: Int) : Result(code)
    }
}