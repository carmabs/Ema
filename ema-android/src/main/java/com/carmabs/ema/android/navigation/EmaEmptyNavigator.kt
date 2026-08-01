package com.carmabs.ema.android.navigation

import android.app.Activity
import android.content.Intent
import androidx.navigation.NavController
import com.carmabs.ema.android.constants.EMA_RESULT_CODE
import com.carmabs.ema.android.constants.EMA_RESULT_KEY
import com.carmabs.ema.core.state.EmaEffect
import com.carmabs.ema.core.navigator.EmaNavigator
import com.google.gson.Gson

data object EmaEmptyNavigationEffect : EmaEffect

class EmaEmptyNavigator constructor(
    private val activity: Activity,
    private val navController: NavController
) : EmaNavigator<EmaEmptyNavigationEffect> {

    private val gson by lazy {
        Gson()
    }

    override fun navigate(effect: EmaEmptyNavigationEffect) = Unit

    override fun navigateBack(result: Any?): Boolean {
        val hasMoreBackScreens = navController.popBackStack()
        if (!hasMoreBackScreens) {
            result?.also {
                activity.setResult(
                    EMA_RESULT_CODE,
                    Intent().putExtra(EMA_RESULT_KEY, gson.toJson(it))
                )
            }
            activity.finish()
        }

        return hasMoreBackScreens
    }
}
