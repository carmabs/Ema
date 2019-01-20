package com.carmabs.ema.presentation.ui.emaway.home

import android.os.Bundle
import androidx.navigation.NavHost
import com.carmabs.ema.R
import com.carmabs.ema.android.navigation.EmaNavigator
import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.ema.core.navigator.EmaBaseNavigator
import com.carmabs.ema.domain.model.User

/**
 * Project: Ema
 * Created by: cmateob on 20/1/19.
 */
class EmaHomeNavigator(override val navHost: NavHost) : EmaNavigator<EmaHomeNavigator.Navigation> {

    sealed class Navigation : EmaNavigationState {

        class User(private val user:com.carmabs.ema.domain.model.User) : Navigation() {
            override fun navigateWith(navigator: EmaBaseNavigator<out EmaNavigationState>) {
                (navigator as? EmaHomeNavigator)?.toUser(user)
            }
        }
    }

    private fun toUser(user: User){
        navHost.navController.navigate(R.id.action_homeViewFragment_to_userFragment,
                Bundle().apply { putSerializable("USER",user) })
    }
}