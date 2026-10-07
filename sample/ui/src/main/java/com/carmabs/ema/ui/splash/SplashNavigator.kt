package com.carmabs.ema.ui.splash

import androidx.fragment.app.Fragment
import com.carmabs.ema.android.navigation.EmaFragmentNavControllerNavigator
import com.carmabs.ema.presentation.splash.SplashEvent
import com.carmabs.ema.sample.ema.R

class SplashNavigator(
    fragment: Fragment
) : EmaFragmentNavControllerNavigator<SplashEvent>(fragment) {

    override fun navigate(event: SplashEvent) {
        when(event){
            is SplashEvent.SplashFinished -> navController.navigate(R.id.action_splashFragment_to_loginFragment)
        }
        
    }
}
