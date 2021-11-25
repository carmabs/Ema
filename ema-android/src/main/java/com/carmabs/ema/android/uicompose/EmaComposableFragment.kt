package com.carmabs.ema.android.uicompose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.lifecycleScope
import com.carmabs.ema.android.ui.EmaBaseFragment
import com.carmabs.ema.core.constants.INT_ZERO
import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.ema.core.state.EmaBaseState
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.viewmodel.EmaViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect


/**
 * Base fragment to bind and unbind view model
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
abstract class EmaComposableFragment<S : EmaBaseState, VM : EmaViewModel<S, NS>, NS : EmaNavigationState> : EmaBaseFragment<S,VM,NS>() {

    private var viewJobs: MutableList<Job> = mutableListOf()

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return ComposeView(requireContext()).apply {
            setContent {
                val state = vm.getObservableState().collectAsState(vm.getCurrentState())
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
            }
        }
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        viewJobs.add(onBindNavigation(lifecycleScope,vm))
        viewJobs.add(onBindSingle(lifecycleScope,vm))
    }

    override fun onStop() {
        onUnbindView(viewJobs)
        super.onStop()

    }

    @Composable
    abstract fun onStateNormalComposable(data:S)

    @Composable
    abstract fun onStateAlternativeComposable(data:EmaExtraData)

    @Composable
    abstract fun onStateErrorComposable(error: Throwable)

    // Discard these methods because they are called now by compose
    final override fun onStateNormal(data: S) {}

    final override fun onStateAlternative(data: EmaExtraData) {}

    final override fun onStateError(error: Throwable) {}
}