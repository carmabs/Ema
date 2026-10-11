package com.carmabs.emanate.ui.splash

import androidx.fragment.app.Fragment
import com.carmabs.emanate.android.navigation.EmaFragmentNavControllerNavigator
import com.carmabs.emanate.presentation.splash.SplashEvent
import com.carmabs.emanate.sample.ui.R

class SplashNavigator(fragment: Fragment) : EmaFragmentNavControllerNavigator<SplashEvent>(fragment) {

    override fun navigate(event: SplashEvent) {
        when (event) {
            is SplashEvent.SplashFinished -> navController.navigate(R.id.action_splashFragment_to_loginFragment)
        }
    }
}
