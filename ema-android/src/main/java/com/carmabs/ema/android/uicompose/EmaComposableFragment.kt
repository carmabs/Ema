package com.carmabs.ema.android.uicompose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.lifecycleScope
import com.carmabs.ema.android.ui.EmaBaseFragment
import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.ema.core.state.EmaBaseState
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.viewmodel.EmaViewModel


/**
 * Base fragment to bind and unbind view model
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
abstract class EmaComposableFragment<S : EmaBaseState, VM : EmaViewModel<S, NS>, NS : EmaNavigationState> : EmaBaseFragment<S,VM,NS>() {

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return ComposeView(requireContext()).apply {
            setContent {
                val state = vm.getObservableState().collectAsState(vm.getCurrentState())
                val singleState = vm.getSingleObservableState().collectAsState(EmaExtraData())
                val emaState = state.value
                onStateNormalComposable(data = emaState.data)
                when(emaState){
                    is EmaState.Alternative -> {
                        onStateAlternativeComposable(data = emaState.dataAlternative)
                    }
                    is EmaState.Error -> {
                        onStateErrorComposable(error = emaState.error)
                    }
                }
                onSingleEventComposable(singleState.value)
            }
        }
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        onBindNavigation(lifecycleScope,vm)
    }

    @Composable
    abstract fun onStateNormalComposable(data:S)

    @Composable
    abstract fun onStateAlternativeComposable(data:EmaExtraData)

    @Composable
    abstract fun onSingleEventComposable(data: EmaExtraData)

    @Composable
    abstract fun onStateErrorComposable(error: Throwable)


    // Discard these methods because they are called now by compose
    final override fun onStateNormal(data: S) {}

    final override fun onStateAlternative(data: EmaExtraData) {}

    final override fun onSingleEvent(data: EmaExtraData) {}

    final override fun onStateError(error: Throwable) {}
}