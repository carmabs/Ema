package com.carmabs.ema.ui.profile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.carmabs.ema.android.extension.getInitializer
import com.carmabs.ema.android.initializer.bundle.strategy.BundleSerializerStrategy
import com.carmabs.ema.android.savestate.EmaSaveStateManager
import com.carmabs.ema.compose.extension.asActionDispatcher
import com.carmabs.ema.compose.extension.createComposableScreen
import com.carmabs.ema.compose.extension.routeId
import com.carmabs.ema.compose.initializer.EmaInitializerSupport
import com.carmabs.ema.presentation.profile.creation.ProfileCreationAction
import com.carmabs.ema.presentation.profile.creation.ProfileCreationEvent
import com.carmabs.ema.presentation.profile.creation.ProfileCreationInitializer
import com.carmabs.ema.presentation.profile.creation.ProfileCreationState
import com.carmabs.ema.presentation.profile.creation.ProfileCreationViewModel
import com.carmabs.ema.presentation.profile.onboarding.ProfileOnBoardingInitializer
import com.carmabs.ema.presentation.profile.onboarding.ProfileOnBoardingViewModel
import com.carmabs.ema.ui.profile.creation.ProfileCreationScreenContent
import com.carmabs.ema.ui.profile.onboarding.ProfileOnBoardingNavigator
import com.carmabs.ema.ui.profile.onboarding.ProfileOnBoardingScreenContent
import com.carmabs.ema.ui.theme.EmaSampleTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get

class ProfileActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            EmaSampleTheme {
                val navController = rememberNavController()
                val navigator = remember {
                    ProfileOnBoardingNavigator(this, navController)
                }

                NavHost(
                    navController = navController,
                    startDestination = ProfileOnBoardingScreenContent::class.routeId
                ) {
                    createComposableScreen(
                        initializerSupport = EmaInitializerSupport.kSerialization(
                            ProfileOnBoardingInitializer.serializer(),
                            getInitializer(
                                BundleSerializerStrategy.kSerialization(
                                    ProfileOnBoardingInitializer.serializer()
                                ),
                                savedInstanceState
                            )
                        ),
                        screenContent = ProfileOnBoardingScreenContent(),
                        onEvent = {
                            navigator.handleProfileOnBoardingEvent(it)
                        },
                        viewModel = { get<ProfileOnBoardingViewModel>() }
                    )
                    createComposableScreen(
                        screenContent = ProfileCreationScreenContent(),
                        viewModel = { get<ProfileCreationViewModel>() },
                        onEvent = {
                            navigator.handleProfileCreationEvent(it)
                        },
                        initializerSupport = EmaInitializerSupport.kSerialization(
                            ProfileCreationInitializer.serializer()
                        ),
                        saveStateManager = EmaSaveStateManager<ProfileCreationState, ProfileCreationEvent> {
                                coroutineScope,
                                savedStateHandle,
                                emaViewModel
                            ->

                            // SAMPLE TO RETAIN STATE THROUGH SAVED STATE HANDLE WHEN PROCESS IS KILLED BY SYSTEM, FOR EXAMPLE,
                            // DENYING A PERMISSION IN SETTINGS

                            val keyName = "USERNAME"
                            val keySurname = "SURNAME"

                            savedStateHandle.get<String>(keyName)?.also {
                                emaViewModel.asActionDispatcher<ProfileCreationAction>()
                                    .dispatch(ProfileCreationAction.UserNameWritten(it))
                            }
                            savedStateHandle.get<String>(keySurname)?.also {
                                emaViewModel.asActionDispatcher<ProfileCreationAction>()
                                    .dispatch(ProfileCreationAction.UserSurnameWritten(it))
                            }
                            coroutineScope.launch {
                                emaViewModel.stateFlow.collect {
                                    savedStateHandle[keyName] = it.name
                                    savedStateHandle[keySurname] = it.surname
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
