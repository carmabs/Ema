package com.carmabs.ema.ui.profile.onboarding

import androidx.activity.ComponentActivity
import androidx.navigation.NavController
import com.carmabs.domain.model.Role
import com.carmabs.ema.android.initializer.EmaInitializerBundle
import com.carmabs.ema.android.initializer.bundle.strategy.BundleSerializerStrategy
import com.carmabs.ema.compose.extension.navigate
import com.carmabs.ema.compose.extension.navigateBack
import com.carmabs.ema.compose.extension.routeId
import com.carmabs.ema.compose.navigation.EmaComposableNavigator
import com.carmabs.ema.presentation.profile.creation.ProfileCreationEvent
import com.carmabs.ema.presentation.profile.creation.ProfileCreationInitializer
import com.carmabs.ema.presentation.profile.onboarding.ProfileOnBoardingEvent
import com.carmabs.ema.ui.profile.creation.ProfileCreationScreenContent

class ProfileOnBoardingNavigator(activity: ComponentActivity, navController: NavController) :
    EmaComposableNavigator(
        context = activity,
        navController = navController
    ) {
    fun handleProfileOnBoardingEvent(event: ProfileOnBoardingEvent) {
        when (event) {
            is ProfileOnBoardingEvent.UserTypeSelected -> {
                navController.navigate(
                    route = ProfileCreationScreenContent::class.routeId,
                    initializerBundle = EmaInitializerBundle(
                        mapToCreationInitializer(event.role),
                        BundleSerializerStrategy.kSerialization(ProfileCreationInitializer.serializer())
                    )
                )
            }

            ProfileOnBoardingEvent.OnBoardingCancelled -> navigateBack()
        }
    }

    private fun mapToCreationInitializer(role: Role) = when (role) {
        Role.ADMIN -> ProfileCreationInitializer.Admin
        Role.BASIC -> ProfileCreationInitializer.UserBasic
    }

    fun handleProfileCreationEvent(event: ProfileCreationEvent) {
        when (event) {
            ProfileCreationEvent.DialogConfirmationAccepted -> navController.navigateBack()
        }
    }
}
