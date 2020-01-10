package com.carmabs.ema.presentation.ui.home

import android.app.Activity
import android.content.Intent
import androidx.navigation.NavController
import com.carmabs.ema.R
import com.carmabs.ema.android.navigation.EmaNavigator
import com.carmabs.ema.core.navigator.EmaBaseNavigator
import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.domain.model.User
import com.carmabs.ema.presentation.ui.error.EmaErrorToolbarViewActivity
import com.carmabs.ema.presentation.ui.user.EmaUserState

/**
 *  *<p>
 * Copyright (c) 2020, Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 *
 * Created by: Carlos Mateo Benito on 20/1/19.
 */
class EmaHomeNavigator(override val navController: NavController, val activity: Activity) : EmaNavigator<EmaHomeNavigator.Navigation> {

    sealed class Navigation : EmaNavigationState {

        class User(private val user: com.carmabs.domain.model.User) : Navigation() {
            override fun navigateWith(navigator: EmaBaseNavigator<out EmaNavigationState>) {
                (navigator as? EmaHomeNavigator)?.toUser(user)
            }
        }

        object Self : Navigation() {
            override fun navigateWith(navigator: EmaBaseNavigator<out EmaNavigationState>) {
                (navigator as? EmaHomeNavigator)?.toSelf()
            }
        }


        object Error: Navigation() {
            override fun navigateWith(navigator: EmaBaseNavigator<out EmaNavigationState>) {
                (navigator as? EmaHomeNavigator)?.toError()
            }
        }
    }

    private fun toSelf() {
        navigateWithAction(
                R.id.action_homeViewFragment_self)
    }

    private fun toUser(user: User) {
        navigateWithAction(
                R.id.action_homeViewFragment_to_userFragment,
                addInputState(EmaUserState(user.name, user.surname))
        )
    }

    private fun toError() {
        navigateToEmaActivityWithResult(activity, Intent(activity,EmaErrorToolbarViewActivity::class.java))
      /*  navigateWithAction(
                R.id.action_homeViewFragment_to_emaErrorViewActivity)*/
    }
}