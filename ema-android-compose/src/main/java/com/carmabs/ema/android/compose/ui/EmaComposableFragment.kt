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
import kotlinx.coroutines.Job


/**
 * Base fragment to bind and unbind view model
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
abstract class EmaComposableFragment<S : EmaBaseState, VM : EmaViewModel<S, D>, D : EmaNavigationState> : EmaBaseFragment<S,VM,D>() {

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
                    is EmaState.Overlayed -> {
                        onStateOverlayedComposable(data = emaState.dataOverlayed)
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

    @CallSuper
    override fun onStop() {
        onUnbindView(viewJobs)
        super.onStop()

    }

    @Composable
    abstract fun onStateNormalComposable(data:S)

    @Composable
    protected open fun onStateOverlayedComposable(data:EmaExtraData){}

    @Composable
    protected open fun onStateErrorComposable(error: Throwable){}

    // Discard these methods because they are called now by compose
    final override fun onStateNormal(data: S) {}

    final override fun onStateOverlayed(data: EmaExtraData) {}

    final override fun onStateError(error: Throwable) {}

    override fun onSingleEvent(data: EmaExtraData) {}
}