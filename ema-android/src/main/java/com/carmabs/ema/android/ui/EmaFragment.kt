package com.carmabs.ema.android.ui

import android.os.Bundle
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.carmabs.ema.android.viewmodel.EmaViewModel
import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.ema.core.state.EmaBaseState


/**
 * Base fragment to bind and unbind view model
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
abstract class EmaFragment<S : EmaBaseState, VM : EmaViewModel<S, NS>, NS : EmaNavigationState> : EmaBaseFragment(), EmaView<S, VM, NS> {

    /**
     * The view model of the fragment
     */
    private var vm: VM? = null

    /**
     * The key id for incoming data through Bundle in fragment instantiation.This is set up when other fragment/activity
     * launches a fragment with arguments provided by Bundle
     */
    abstract val inputStateKey: String?

    /**
     * Called once the view model is instantiated
     * @param viewModel instantiated
     */
    abstract fun onInitialized(viewModel: VM)

    /**
     * The incoming state in fragment instantiation. This is set up when other fragment/activity
     * launches a fragment with arguments provided by Bundle
     */
    override val inputState: S? by lazy { getInState() }


    /**
     * The view model is instantiated on fragment creation
     * @param view which inflated the fragment
     * @param savedInstanceState saved data for recreation
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            initializeViewModel(it,
                    if (fragmentViewModelScope)
                        this
                    else
                        null)
        }

    }

    /**
     * Determine if the view model lifecycle is attached to the Activity or to the Fragment
     */
    abstract val fragmentViewModelScope: Boolean

    /**
     * Methods called when view model has been created
     * @param viewModel
     */
    override fun onViewModelInitalized(viewModel: VM) {
        vm = viewModel
        onInitialized(viewModel)
    }

    /**
     * Destroy the view and unbind the observers from view model
     */
    override fun onDestroyView() {
        super.onDestroyView()
        val owner: LifecycleOwner = if (fragmentViewModelScope) this else activity!!
        vm?.unBindObservables(owner)
    }

    /**
     * Get the incoming state from another fragment/activity by the key [inputStateKey] provided
     */
    private fun getInState(): S? {
        return arguments?.let {
            if (it.containsKey(inputStateKey)) {
                it.get(inputStateKey) as? S

            } else
                null
        }
    }

    fun setInputState(inState: S) {
        arguments = Bundle().apply { putSerializable(inputStateKey, inState) }
    }
}