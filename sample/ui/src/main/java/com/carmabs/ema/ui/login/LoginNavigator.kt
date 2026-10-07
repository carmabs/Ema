package com.carmabs.ema.ui.login

import androidx.fragment.app.Fragment
import com.carmabs.ema.android.extension.navigate
import com.carmabs.ema.android.initializer.EmaInitializerBundle
import com.carmabs.ema.android.initializer.bundle.strategy.BundleSerializerStrategy
import com.carmabs.ema.android.navigation.EmaFragmentNavControllerNavigator
import com.carmabs.ema.presentation.home.HomeInitializer
import com.carmabs.ema.presentation.login.LoginEvent
import com.carmabs.ema.sample.ema.R

class LoginNavigator(fragment: Fragment) : EmaFragmentNavControllerNavigator<LoginEvent>(fragment) {

    override fun navigate(event: LoginEvent) {
        when (event) {
            is LoginEvent.LoginSuccess -> {
                navController.navigate(
                    id = R.id.action_loginFragment_to_homeFragment,
                    initializerBundle = EmaInitializerBundle(
                        mapToHomeInitializer(event),
                        BundleSerializerStrategy.kSerialization(HomeInitializer.serializer())
                    )
                )
            }

            is LoginEvent.Message,
            is LoginEvent.LastUserAdded -> Unit
        }
    }

    private fun mapToHomeInitializer(navigationEvent: LoginEvent.LoginSuccess): HomeInitializer =
        navigationEvent.user.let {
            HomeInitializer.HomeUser(it)
        }
}
