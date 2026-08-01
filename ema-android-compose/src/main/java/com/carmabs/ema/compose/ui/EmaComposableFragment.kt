package com.carmabs.ema.compose.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import com.carmabs.ema.android.base.EmaCoreFragment
import com.carmabs.ema.core.state.EmaEvent
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.viewmodel.EmaViewModel


/**
 * Base fragment to bind and unbind view model
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
abstract class EmaComposableFragment<S : EmaState, VM : EmaViewModel<S, E>, E : EmaEvent>
    : EmaCoreFragment<S, VM, E>() {

    protected var isFirstNormalExecution: Boolean = true
        private set

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        isFirstNormalExecution = true
        return ComposeView(requireContext()).apply {
            setContent {
                val state = viewModel.stateFlow
                    .collectAsState(initial = viewModel.initialState)
                if (viewModel.shouldRenderState) {
                    onRenderState(state = state.value)
                    isFirstNormalExecution = false
                }
            }
        }
    }

    // Discard these methods because they are called now by compose
    final override fun onState(state: S) = Unit

    @Composable
    abstract fun onRenderState(state: S)


}
