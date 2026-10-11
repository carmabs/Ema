package com.carmabs.emanate.ui.profile.onboarding

import androidx.activity.ComponentActivity
import androidx.navigation.NavController
import com.carmabs.domain.model.Role
import com.carmabs.emanate.android.initializer.EmaInitializerBundle
import com.carmabs.emanate.android.initializer.bundle.strategy.BundleSerializerStrategy
import com.carmabs.emanate.compose.extension.navigate
import com.carmabs.emanate.compose.extension.navigateBack
import com.carmabs.emanate.compose.extension.routeId
import com.carmabs.emanate.compose.navigation.EmaComposableNavigator
import com.carmabs.emanate.presentation.profile.creation.ProfileCreationEvent
import com.carmabs.emanate.presentation.profile.creation.ProfileCreationInitializer
import com.carmabs.emanate.presentation.profile.onboarding.ProfileOnBoardingEvent
import com.carmabs.emanate.ui.profile.creation.ProfileCreationScreenContent

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
