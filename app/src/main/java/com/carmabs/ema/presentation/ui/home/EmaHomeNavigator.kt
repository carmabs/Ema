package com.carmabs.ema.presentation.ui.home

import androidx.navigation.NavController
import com.carmabs.ema.R
import com.carmabs.ema.android.navigation.EmaNavigator
import com.carmabs.ema.core.navigator.EmaBaseNavigator
import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.domain.model.User
import com.carmabs.ema.presentation.ui.user.EmaUserState

/**
 * Project: Ema
 * Created by: cmateob on 20/1/19.
 */
class EmaHomeNavigator(override val navController: NavController) : EmaNavigator<EmaHomeNavigator.Navigation> {

    sealed class Navigation : EmaNavigationState {

        class User(private val user: com.carmabs.domain.model.User) : Navigation() {
            override fun navigateWith(navigator: EmaBaseNavigator<out EmaNavigationState>) {
                (navigator as? EmaHomeNavigator)?.toUser(user)
            }
        }

        object Error: Navigation() {
            override fun navigateWith(navigator: EmaBaseNavigator<out EmaNavigationState>) {
                (navigator as? EmaHomeNavigator)?.toError()
            }
        }
    }

    private fun toUser(user: User) {
        navigateWithAction(
                R.id.action_homeViewFragment_to_userFragment,
                addInputState(EmaUserState(user.name, user.surname))
        )
    }

    private fun toError() {
        navigateWithAction(
                R.id.action_homeViewFragment_to_emaErrorViewActivity)
    }
}