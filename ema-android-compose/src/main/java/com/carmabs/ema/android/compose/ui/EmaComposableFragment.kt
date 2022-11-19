package com.carmabs.ema.android.compose.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.carmabs.ema.android.ui.EmaFragment
import com.carmabs.ema.core.navigator.EmaDestination
import com.carmabs.ema.core.state.EmaDataState
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.viewmodel.EmaViewModel
import kotlinx.coroutines.Job


/**
 * Base fragment to bind and unbind view model
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
abstract class EmaComposableFragment<DS : EmaDataState, S : EmaState<DS>, VM : EmaViewModel<DS, D>, D : EmaDestination> :
    EmaFragment<ViewBinding, DS, VM, D>() {

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
                when (emaState) {
                    is EmaState.Normal -> {
                        /*DO NOTHING, ALREADY HANDLED*/
                    }
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
        viewJobs.add(onBindNavigation(lifecycleScope, vm))
        viewJobs.add(onBindSingle(lifecycleScope, vm))
    }

    @CallSuper
    override fun onStop() {
        onUnbindView(viewJobs, vm)
        super.onStop()

    }

    @Composable
    abstract fun onStateNormalComposable(data: DS)

    @Composable
    protected open fun onStateOverlayedComposable(data: EmaExtraData) {
    }

    @Composable
    protected open fun onStateErrorComposable(error: Throwable) {
    }

    // Discard these methods because they are called now by compose
    override fun ViewBinding.onStateNormal(data: DS) = Unit

    override fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?): ViewBinding {
        TODO("Not yet implemented")
    }
}