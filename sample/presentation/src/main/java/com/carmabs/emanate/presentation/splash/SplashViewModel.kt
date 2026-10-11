package com.carmabs.emanate.presentation.splash

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.state.EmaState
import com.carmabs.emanate.presentation.base.BaseViewModel
import kotlinx.coroutines.delay

class SplashViewModel : BaseViewModel<EmaState.EMPTY, EmaAction.EMPTY, SplashEvent>(EmaState.EMPTY) {
    override fun onAction(action: EmaAction.EMPTY) = Unit

    override fun onStateCreated(initializer: EmaAction.Initializer?) {
        sideEffect {
            delay(1500)
            postEvent(SplashEvent.SplashFinished)
        }
    }
}
