package com.carmabs.emanate.presentation.splash

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.source.SourceSetupScope
import com.carmabs.emanate.core.state.EmaState
import com.carmabs.emanate.presentation.base.BaseViewModel
import kotlinx.coroutines.delay

class SplashViewModel : BaseViewModel<EmaState.EMPTY, EmaAction.EMPTY, SplashEvent>(EmaState.EMPTY) {

    override fun SourceSetupScope<EmaState.EMPTY, EmaAction.EMPTY, SplashEvent>.setup() {
        // The view is created
        addSideEffect<EmaAction.Initializer> {
            delay(1500)
            postEvent(SplashEvent.SplashFinished)
        }
    }
}
