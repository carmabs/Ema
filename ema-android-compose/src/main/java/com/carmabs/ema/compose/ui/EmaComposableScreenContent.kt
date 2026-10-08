package com.carmabs.ema.compose.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import com.carmabs.ema.compose.action.EmaImmutableActionDispatcher
import com.carmabs.ema.core.action.EmaAction
import com.carmabs.ema.core.state.EmaEvent
import com.carmabs.ema.core.state.EmaState

interface EmaComposableScreenContent<S : EmaState, A : EmaAction, E : EmaEvent> {

    suspend fun onEvent(context: Context, event: E, actions: EmaImmutableActionDispatcher<A>) = Unit

    /**
     * Action to dispatch when back is pressed. The view model decides how to react to it.
     * @param state current state of the screen
     * @return the action to dispatch, or null to let the system handle the back press
     */
    fun onBack(state: S): A? = null

    @Composable
    @SuppressLint("ComposableNaming")
    fun onState(state: S, actions: EmaImmutableActionDispatcher<A>)
}
