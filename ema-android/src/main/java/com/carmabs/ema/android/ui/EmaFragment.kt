package com.carmabs.ema.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.viewbinding.ViewBinding
import com.carmabs.ema.android.extension.addOnBackPressedListener
import com.carmabs.ema.core.navigator.EmaNavigationTarget
import com.carmabs.ema.core.state.EmaBaseState
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.core.viewmodel.EmaViewModel
import kotlinx.coroutines.Job


/**
 * Base fragment to bind and unbind view model
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
abstract class EmaFragment<B : ViewBinding, S : EmaBaseState, VM : EmaViewModel<S, NS>, NS : EmaNavigationTarget> :
    EmaBaseFragment<S, VM, NS>() {

    private var _binding: B? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    protected val binding
        get() = _binding!!

    private var viewJob: MutableList<Job>? = null

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        previousState = null
        _binding = createViewBinding(inflater, container)
        return binding.root
    }

    /**
     * Method to provide the fragment ViewBinding class to represent the layout.
     */
    abstract fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?): B

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addOnBackPressedListener {
            vm.onActionHardwareBackPressed()
        }
    }


    @CallSuper
    override fun onStart() {
        viewJob = onBindView(
            getScope(),
            vm
        )
        super.onStart()
    }

    @CallSuper
    override fun onStop() {
        onUnbindView(viewJob)
        super.onStop()
    }

    final override fun onStateNormal(data: S) {
        binding.onStateNormal(data)
    }

    final override fun onStateOverlayed(data: EmaExtraData) {
        binding.onStateOverlayed(data)
    }

    final override fun onSingleEvent(data: EmaExtraData) {
        binding.onSingleEvent(data)
    }

    final override fun onStateError(error: Throwable) {
        binding.onStateError(error)
    }

    @CallSuper
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    abstract fun B.onStateNormal(data: S)
    protected open fun B.onStateOverlayed(data: EmaExtraData) {}
    protected open fun B.onStateError(throwable: Throwable) {}
    protected open fun B.onSingleEvent(data: EmaExtraData) {}
}