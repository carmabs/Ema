package com.carmabs.ema.ui.home

import androidx.fragment.app.Fragment
import com.carmabs.ema.android.extension.navigate
import com.carmabs.ema.android.initializer.EmaInitializerBundle
import com.carmabs.ema.android.initializer.bundle.strategy.BundleSerializerStrategy
import com.carmabs.ema.android.navigation.EmaFragmentNavControllerNavigator
import com.carmabs.ema.presentation.home.HomeEvent
import com.carmabs.ema.presentation.profile.onboarding.ProfileOnBoardingInitializer
import com.carmabs.ema.sample.ema.R

class HomeNavigator(fragment: Fragment) : EmaFragmentNavControllerNavigator<HomeEvent>(fragment) {

    override fun navigate(event: HomeEvent) {
        when (event) {
            is HomeEvent.ProfileClicked -> {
                navController.navigate(
                    id = R.id.action_homeFragment_to_profileActivity,
                    initializerBundle = EmaInitializerBundle(
                        ProfileOnBoardingInitializer.Default(event.user.name),
                        BundleSerializerStrategy.kSerialization(ProfileOnBoardingInitializer.serializer())
                    )
                )
            }
        }
    }
}
