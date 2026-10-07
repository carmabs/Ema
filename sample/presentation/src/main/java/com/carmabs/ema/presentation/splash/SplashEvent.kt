package com.carmabs.ema.presentation.splash

import com.carmabs.ema.core.state.EmaEvent

sealed interface SplashEvent : EmaEvent {
    data object SplashFinished : SplashEvent
}
