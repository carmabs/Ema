package com.carmabs.emanate.ui.home

import androidx.fragment.app.Fragment
import com.carmabs.emanate.android.extension.navigate
import com.carmabs.emanate.android.initializer.EmaInitializerBundle
import com.carmabs.emanate.android.initializer.bundle.strategy.BundleSerializerStrategy
import com.carmabs.emanate.android.navigation.EmaFragmentNavControllerNavigator
import com.carmabs.emanate.presentation.home.HomeEvent
import com.carmabs.emanate.presentation.profile.onboarding.ProfileOnBoardingInitializer
import com.carmabs.emanate.sample.ui.R

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
