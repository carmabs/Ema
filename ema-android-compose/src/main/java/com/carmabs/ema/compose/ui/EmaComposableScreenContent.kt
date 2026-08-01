package com.carmabs.ema.compose.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import com.carmabs.ema.compose.action.EmaImmutableActionDispatcher
import com.carmabs.ema.core.action.EmaAction
import com.carmabs.ema.core.state.EmaEffect
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.core.state.EmaState

interface EmaComposableScreenContent<S : EmaState, A : EmaAction.Screen> {

    suspend fun onEffect(
        context: Context,
        effect: EmaEffect,
        actions: EmaImmutableActionDispatcher<A>
    ) = Unit

    @Composable
    @SuppressLint("ComposableNaming")
    fun onState(state: S, actions: EmaImmutableActionDispatcher<A>)
}
