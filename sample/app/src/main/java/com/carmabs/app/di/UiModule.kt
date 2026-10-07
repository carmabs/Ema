package com.carmabs.app.di

import androidx.fragment.app.FragmentManager
import com.carmabs.ema.presentation.home.HomeState
import com.carmabs.ema.presentation.home.HomeViewModel
import com.carmabs.ema.presentation.login.LoginState
import com.carmabs.ema.presentation.login.LoginViewModel
import com.carmabs.ema.presentation.profile.creation.ProfileCreationState
import com.carmabs.ema.presentation.profile.creation.ProfileCreationViewModel
import com.carmabs.ema.presentation.profile.onboarding.ProfileOnBoardingState
import com.carmabs.ema.presentation.profile.onboarding.ProfileOnBoardingViewModel
import com.carmabs.ema.presentation.splash.SplashViewModel
import com.carmabs.ema.ui.dialog.AppDialogProvider
import com.carmabs.ema.ui.dialog.error.ErrorDialogProvider
import com.carmabs.ema.ui.dialog.loading.LoadingDialogProvider
import com.carmabs.ema.ui.dialog.simple.SimpleDialogProvider
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

    factory { SplashViewModel() }
    factoryOf(::LoginViewModel)
    factoryOf(::HomeViewModel)
    factoryOf(::ProfileOnBoardingViewModel)
    factoryOf(::ProfileCreationViewModel)

    factory { LoginState.DEFAULT }
    factory { HomeState.DEFAULT }
    factory { ProfileOnBoardingState.DEFAULT }
    factory { ProfileCreationState.DEFAULT }
}
