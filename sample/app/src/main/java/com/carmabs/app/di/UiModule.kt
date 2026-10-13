package com.carmabs.app.di

import androidx.fragment.app.FragmentManager
import com.carmabs.emanate.presentation.choice.ImplementationChoiceViewModel
import com.carmabs.emanate.presentation.choice.ViewModelImplementationSelector
import com.carmabs.emanate.presentation.home.HomeActionViewModel
import com.carmabs.emanate.presentation.home.HomeState
import com.carmabs.emanate.presentation.home.HomeViewModel
import com.carmabs.emanate.presentation.login.LoginActionViewModel
import com.carmabs.emanate.presentation.login.LoginState
import com.carmabs.emanate.presentation.login.LoginViewModel
import com.carmabs.emanate.presentation.profile.creation.ProfileCreationActionViewModel
import com.carmabs.emanate.presentation.profile.creation.ProfileCreationState
import com.carmabs.emanate.presentation.profile.creation.ProfileCreationViewModel
import com.carmabs.emanate.presentation.profile.onboarding.ProfileOnBoardingActionViewModel
import com.carmabs.emanate.presentation.profile.onboarding.ProfileOnBoardingState
import com.carmabs.emanate.presentation.profile.onboarding.ProfileOnBoardingViewModel
import com.carmabs.emanate.presentation.splash.SplashActionViewModel
import com.carmabs.emanate.presentation.splash.SplashViewModel
import com.carmabs.emanate.ui.dialog.AppDialogProvider
import com.carmabs.emanate.ui.dialog.error.ErrorDialogProvider
import com.carmabs.emanate.ui.dialog.loading.LoadingDialogProvider
import com.carmabs.emanate.ui.dialog.simple.SimpleDialogProvider
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val uiModule = module {

    factory { (fragmentManager: FragmentManager) ->
        AppDialogProvider(
            fragmentManager,
            SimpleDialogProvider(fragmentManager),
            LoadingDialogProvider(fragmentManager),
            ErrorDialogProvider(fragmentManager)
        )
    }

    // Implementation of the view models chosen in the first screen
    single { ViewModelImplementationSelector() }
    factoryOf(::ImplementationChoiceViewModel)

    // Built with EmaViewModel
    factory { SplashViewModel() }
    factoryOf(::LoginViewModel)
    factoryOf(::HomeViewModel)
    factoryOf(::ProfileOnBoardingViewModel)
    factoryOf(::ProfileCreationViewModel)

    // Built with EmaViewModelAction
    factory { SplashActionViewModel() }
    factoryOf(::LoginActionViewModel)
    factoryOf(::HomeActionViewModel)
    factoryOf(::ProfileOnBoardingActionViewModel)
    factoryOf(::ProfileCreationActionViewModel)

    factory { LoginState.DEFAULT }
    factory { HomeState.DEFAULT }
    factory { ProfileOnBoardingState.DEFAULT }
    factory { ProfileCreationState.DEFAULT }
}
