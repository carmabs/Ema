package com.carmabs.emanate.presentation.splash

import com.carmabs.emanate.core.state.EmaEvent

sealed interface SplashEvent : EmaEvent {
    data object SplashFinished : SplashEvent
}
