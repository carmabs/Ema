package com.carmabs.ema.compose.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import com.carmabs.ema.compose.action.EmaImmutableActionDispatcher
import com.carmabs.ema.core.action.EmaAction
import com.carmabs.ema.core.state.EmaEvent
import com.carmabs.ema.core.state.EmaState

interface EmaComposableScreenContent<S : EmaState, A : EmaAction.Screen, E: EmaEvent> {

    suspend fun onEffect(
        context: Context,
        effect: E,
        actions: EmaImmutableActionDispatcher<A>
    ) = Unit

    @Composable
    @SuppressLint("ComposableNaming")
    fun onState(state: S, actions: EmaImmutableActionDispatcher<A>)
}
