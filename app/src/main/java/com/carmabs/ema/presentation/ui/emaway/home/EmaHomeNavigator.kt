package com.carmabs.ema.presentation.ui.emaway.home

import androidx.navigation.NavController
import androidx.navigation.NavHost
import com.carmabs.ema.R
import com.carmabs.ema.android.navigation.EmaNavigator
import com.carmabs.ema.core.navigator.EmaBaseNavigator
import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.ema.domain.model.User
import com.carmabs.ema.presentation.ui.emaway.user.EmaUserState

/**
 * Project: Ema
 * Created by: cmateob on 20/1/19.
 */
class EmaHomeNavigator(override val navController: NavController) : EmaNavigator<EmaHomeNavigator.Navigation> {

    sealed class Navigation : EmaNavigationState {

        class User(private val user: com.carmabs.ema.domain.model.User) : Navigation() {
            override fun navigateWith(navigator: EmaBaseNavigator<out EmaNavigationState>) {
                (navigator as? EmaHomeNavigator)?.toUser(user)
            }
        }
    }

    private fun toUser(user: User) {
        navigateWithAction(
                R.id.action_homeViewFragment_to_userFragment,
                addInputState(EmaUserState(user.name,user.surname))
        )
    }
}