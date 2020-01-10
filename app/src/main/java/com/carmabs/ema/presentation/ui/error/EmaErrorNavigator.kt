package com.carmabs.ema.presentation.ui.error

import android.app.Activity
import android.content.Intent
import androidx.navigation.NavController
import com.carmabs.ema.android.navigation.EmaNavigator
import com.carmabs.ema.core.navigator.EmaBaseNavigator
import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.ema.presentation.ui.backdata.EmaBackToolbarActivity

/**
 *  *<p>
 * Copyright (c) 2020, Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 *
 * Created by: Carlos Mateo Benito on 20/1/19.
 */
class EmaErrorNavigator(
        override val navController: NavController,
        private val activity: Activity
) : EmaNavigator<EmaErrorNavigator.Navigation> {

    sealed class Navigation : EmaNavigationState {

        object BackUser : Navigation() {
            override fun navigateWith(navigator: EmaBaseNavigator<out EmaNavigationState>) {
                (navigator as? EmaErrorNavigator)?.toBackUser()
            }
        }
    }

    private fun toBackUser() {
        navigateToEmaActivityWithResult(activity, Intent(activity,EmaBackToolbarActivity::class.java))
    }
}