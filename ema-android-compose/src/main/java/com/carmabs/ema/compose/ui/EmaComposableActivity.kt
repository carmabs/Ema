package com.carmabs.ema.compose.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.carmabs.ema.android.base.EmaCoreActivity
import com.carmabs.ema.core.state.EmaEvent
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.viewmodel.EmaViewModel


/**
 * Base fragment to bind and unbind view model
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
abstract class EmaComposableActivity<S : EmaState, VM : EmaViewModel<S,E>, E : EmaEvent>
    : EmaCoreActivity<S, VM, E>() {

    protected var isFirstNormalExecution: Boolean = true
        private set

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isFirstNormalExecution = true
        setContent {
            val state = viewModel.stateFlow
                .collectAsState(initial = viewModel.initialState)
            if (viewModel.shouldRenderState) {
                onRenderState(state = state.value)
                isFirstNormalExecution = false
            }
        }
    }

    @Composable
    abstract fun onRenderState(state: S)

    // Discard these methods because they are called now by compose
    final override fun onState(state: S) = Unit
}
