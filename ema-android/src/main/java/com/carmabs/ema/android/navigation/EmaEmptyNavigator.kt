package com.carmabs.ema.android.navigation

import android.app.Activity
import androidx.navigation.NavController
import com.carmabs.ema.core.navigator.EmaEmptyDestination
import com.carmabs.ema.core.navigator.EmaNavigator


class EmaEmptyNavigator constructor(
    private val activity: Activity,
    private val navController: NavController
) : EmaNavigator<EmaEmptyDestination> {

    override fun navigate(destination: EmaEmptyDestination) = false

    override fun navigateBack(): Boolean {
        val hasMoreBackScreens = navController.popBackStack()
        if (!hasMoreBackScreens)
            activity.finish()

        return hasMoreBackScreens
    }
}