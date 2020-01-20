package com.carmabs.ema.android.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.carmabs.ema.android.extra.EmaReceiverModel
import com.carmabs.ema.android.extra.EmaResultModel
import com.carmabs.ema.android.viewmodel.EmaFactory
import com.carmabs.ema.android.viewmodel.EmaViewModel
import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.ema.core.state.EmaBaseState
import com.carmabs.ema.core.state.EmaState


/**
 * Base fragment to bind and unbind view model
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
abstract class EmaFragment<S : EmaBaseState, VM : EmaViewModel<S, NS>, NS : EmaNavigationState> : EmaBaseFragment(), EmaView<S, VM, NS> {

    /**
     * The view model of the fragment
     */
    private lateinit var vm: VM

    /**
     * The key id for incoming data through Bundle in fragment instantiation.This is set up when other fragment/activity
     * launches a fragment with arguments provided by Bundle
     */
    protected open val inputStateKey: String by lazy {
        vm.initialViewState.javaClass.name
    }

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
     * The list which handles the extra view models attached, to unbind the observers
     * when the view fragment is destroyed
     */
    private val extraViewModelList: MutableList<EmaViewModel<*, *>> by lazy { mutableListOf<EmaViewModel<*, *>>() }

    /**
     * The view model is instantiated on fragment resume.
     */
    override fun onResume() {
        super.onResume()
        activity?.let {
            initializeViewModel(it,
                    if (fragmentViewModelScope)
                        this
                    else
                        null)
        }
    }

    protected open fun provideToolbarTitle(): String? = null

    /**
     * Previous state for comparing state properties update
     */
    override var previousState: S? = null
    /**
     * Add a view model observer to current fragment
     * @param viewModelAttachedSeed is the view model seed will used as factory instance if there is no previous
     * view model retained by the OS
     * @param fragment the fragment scope
     * @param fragmentActivity the activity scope, if it is provided this will be the scope of the view model attached
     * @param observerFunction the observer of the view model attached
     * @return The view model attached
     */
    protected fun <AS, VM : EmaViewModel<AS, *>> addExtraViewModel(
            viewModelAttachedSeed: VM,
            fragment: Fragment,
            fragmentActivity: FragmentActivity? = null,
            observerFunction: ((attachedState: EmaState<AS>) -> Unit)? = null): VM {

        val viewModel =
                fragmentActivity?.let {
                    ViewModelProviders.of(it, EmaFactory(viewModelAttachedSeed))[viewModelAttachedSeed::class.java]
                }
                        ?: ViewModelProviders.of(fragment, EmaFactory(viewModelAttachedSeed))[viewModelAttachedSeed::class.java]

        observerFunction?.also { viewModel.getObservableState().observe(this, Observer(it)) }
        extraViewModelList.add(viewModel)

        return viewModel
    }

    /**
     * Determine if the view model lifecycle is attached to the Activity or to the Fragment
     */
    abstract val fragmentViewModelScope: Boolean

    /**
     * Methods called when view model has been created
     * @param viewModel
     */
    final override fun onViewModelInitialized(viewModel: VM) {
        vm = viewModel
        onInitialized(viewModel)
    }

    /**
     * Destroy the view and unbind the observers from view model
     */
    override fun onPause() {
        super.onPause()
        val owner: LifecycleOwner = if (fragmentViewModelScope) this else requireActivity()
        removeExtraViewModels()
        vm.unBindObservables(owner)
        vm.resultViewModel.unBindObservables(this)

    }

    /**
     * Remove extra view models attached
     */
    private fun removeExtraViewModels() {
        extraViewModelList.forEach {
            it.unBindObservables(this)
        }
        extraViewModelList.clear()
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

    /**
     * Override to do logic if it is required when result is setted
     */
    override fun onResultSetEvent(emaResultModel: EmaResultModel) {

    }

    /**
     * Override to do logic if it is required when result receiver is invoked
     */
    override fun onResultReceiverInvokeEvent(emaReceiverModel: EmaReceiverModel) {

    }
}