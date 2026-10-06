package com.carmabs.ema.presentation.ui.splash

import com.carmabs.ema.core.state.EmaEvent

sealed interface SplashEvent : EmaEvent {
    data object SplashFinished : SplashEvent
}
