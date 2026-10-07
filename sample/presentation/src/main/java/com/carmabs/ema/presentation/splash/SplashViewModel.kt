package com.carmabs.ema.presentation.splash

import com.carmabs.ema.core.action.EmaAction
import com.carmabs.ema.core.initializer.EmaInitializer
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.presentation.base.BaseViewModel
import kotlinx.coroutines.delay

class SplashViewModel : BaseViewModel<EmaState.EMPTY, EmaAction.Screen.EMPTY, SplashEvent>(EmaState.EMPTY) {
    override fun onAction(action: EmaAction.Screen.EMPTY) = Unit

    override fun onStateCreated(initializer: EmaInitializer?) {
        sideEffect {
            delay(1500)
            postEvent(SplashEvent.SplashFinished)
        }
    }
}
