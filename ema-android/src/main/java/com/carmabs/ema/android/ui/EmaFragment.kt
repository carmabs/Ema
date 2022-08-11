package com.carmabs.ema.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.carmabs.ema.android.delegates.emaViewModelDelegate
import com.carmabs.ema.android.extension.addOnBackPressedListener
import com.carmabs.ema.android.navigation.EmaFragmentNavControllerNavigator
import com.carmabs.ema.android.viewmodel.EmaAndroidViewModel
import com.carmabs.ema.android.viewmodel.EmaFactory
import com.carmabs.ema.core.initializer.EmaInitializer
import com.carmabs.ema.core.navigator.EmaNavigationTarget
import com.carmabs.ema.core.state.EmaBaseState
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.view.EmaViewModelTrigger
import com.carmabs.ema.core.viewmodel.EmaViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.fragmentScope
import org.koin.core.scope.Scope


/**
 *
 * Abstract base class to implement Kodein framework in fragment context
 * to handle dependency injection
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
abstract class EmaFragment<B : ViewBinding, S : EmaBaseState, VM : EmaViewModel<S, NT>, NT : EmaNavigationTarget> :
    Fragment(), EmaAndroidView<S, VM, NT>, AndroidScopeComponent {

    final override val scope: Scope by fragmentScope()

    private var _binding: B? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    protected val binding
        get() = _binding!!

    private var viewJob: MutableList<Job>? = null

    private val extraViewJobs: MutableList<Job> by lazy {
        mutableListOf()
    }

    protected var isFirstNormalExecution: Boolean = true
        private set

    protected var isFirstOverlayedExecution: Boolean = true
        private set

    protected var isFirstErrorExecution: Boolean = true
        private set

    /**
     * The view model of the fragment
     */
    protected val vm: VM by emaViewModelDelegate()

    /**
     * Trigger to start viewmodel only when startViewModel is launched
     */
    override val startTrigger: EmaViewModelTrigger? = null

    /**
     * Automatically updates previousState
     */
    override val updatePreviousStateAutomatically: Boolean = true

    /**
     * The incoming initializer in fragment instantiation. This is set up when other fragment/activity
     * launches a fragment with arguments provided by Bundle
     */
    override val initializer: EmaInitializer? by lazy { getInitializerArgument() }

    /**
     * The list which handles the extra view models attached, to unbind the observers
     * when the view fragment is destroyed
     */
    private val extraViewModelList: MutableList<EmaAndroidViewModel<VM>> by lazy { mutableListOf() }

    protected open fun provideToolbarTitle(): String? = null

    /**
     * Previous state for comparing state properties update
     */
    override var previousState: S? = null



    /**
     * Method to provide the fragment ViewBinding class to represent the layout.
     */
    abstract fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?): B

    abstract override val navigator: EmaFragmentNavControllerNavigator<NT>?

    abstract fun provideAndroidViewModel(): EmaAndroidViewModel<VM>


    final override val androidViewModelSeed: EmaAndroidViewModel<VM> by lazy {
        provideAndroidViewModel()
    }

    final override val viewModelSeed: VM
        get() = androidViewModelSeed.emaViewModel

    override val coroutineScope: CoroutineScope
        get() = lifecycleScope

    /**
     * Remove extra view models attached
     */
    private fun removeExtraViewModels() {
        extraViewJobs.forEach {
            it.cancel()
        }
        extraViewJobs.clear()
        extraViewModelList.clear()
    }


    fun setInitializer(initializer: EmaInitializer) {
        arguments = Bundle().apply { putSerializable(EmaInitializer.KEY, initializer) }
    }

    /**
     * Get the incoming initializer from another fragment/activity by the key [inputStateKey] provided
     */
    private fun getInitializerArgument(): EmaInitializer? {
        return arguments?.let {
            if (it.containsKey(EmaInitializer.KEY)) {
                it.get(EmaInitializer.KEY) as? EmaInitializer
            } else
                null
        }
    }

    /**
     * Get the scope of the fragment depending the viewModelScopeSelected
     */
    protected fun getScope(): CoroutineScope {
        return if (fragmentViewModelScope)
            coroutineScope
        else
            requireActivity().lifecycleScope
    }

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isFirstNormalExecution = true
        isFirstOverlayedExecution = true
        isFirstErrorExecution = true
        previousState = null
        _binding = createViewBinding(inflater, container)
        return binding.root
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addOnBackPressedListener {
            vm.onActionHardwareBackPressed()
        }
    }


    /**
     * The view model is instantiated on fragment resume.
     */
    override fun onStart() {
        super.onStart()
        viewJob = onBindView(
            getScope(),
            vm
        )
        onStartView(vm)
    }

    /**
     * Notifies the view model that view has been gone to foreground.
     */
    @CallSuper
    override fun onResume() {
        super.onResume()
        onResumeView(vm)
    }

    /**
     * Notifies the view model that view has been gone to background.
     */
    @CallSuper
    override fun onPause() {
        onPauseView(vm)
        super.onPause()
    }

    /**
     * Add a view model observer to current fragment
     * @param viewModelAttachedSeed is the view model seed will used as factory instance if there is no previous
     * view model retained by the OS
     * @param fragment the fragment scope
     * @param fragmentActivity the activity scope, if it is provided this will be the scope of the view model attached
     * @param observerFunction the observer of the view model attached
     * @return The view model attached
     */
    fun <AVM : EmaAndroidViewModel<out EmaViewModel<*, *>>> addExtraViewModel(
        viewModelAttachedSeed: AVM,
        fragment: Fragment,
        fragmentActivity: FragmentActivity? = null,
        observerFunction: ((attachedState: EmaState<*>) -> Unit)? = null
    ): AVM {
        val viewModel =
            fragmentActivity?.let {
                ViewModelProvider(
                    it,
                    EmaFactory(viewModelAttachedSeed)
                )[viewModelAttachedSeed::class.java]
            }
                ?: ViewModelProvider(
                    fragment,
                    EmaFactory(viewModelAttachedSeed)
                )[viewModelAttachedSeed::class.java]

        observerFunction?.also {
            val job = coroutineScope.launch {
                viewModel.emaViewModel.getObservableState().collect {
                    observerFunction.invoke(it)
                }
            }
            extraViewJobs.add(job)
        }
        extraViewModelList.add(viewModel as EmaAndroidViewModel<VM>)

        return viewModel
    }

    /**
     * Determine if the view model lifecycle is attached to the Activity or to the Fragment
     */
    open val fragmentViewModelScope: Boolean = true

    @CallSuper
    override fun onStop() {
        super.onStop()
        onUnbindView(viewJob)
        removeExtraViewModels()
    }

    @CallSuper
    override fun onDestroyView() {
        previousState = null
        super.onDestroyView()
    }

    @CallSuper
    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }



    @CallSuper
    override fun onNavigation(navigation: NT?) {
        super.onNavigation(navigation)
    }

    final override fun onEmaStateNormal(data: S) {
        binding.onStateNormal(data)
        isFirstNormalExecution = false
    }

    final override fun onEmaStateOverlayed(data: EmaExtraData) {
        binding.onStateOverlayed(data)
        isFirstOverlayedExecution = false
    }

    final override fun onEmaStateError(error: Throwable) {
        binding.onStateError(error)
        isFirstErrorExecution = false
    }

    final override fun onSingleEvent(data: EmaExtraData) {
        binding.onSingleEvent(data)
    }

    abstract fun B.onStateNormal(data: S)
    protected open fun B.onStateOverlayed(data: EmaExtraData) {}
    protected open fun B.onStateError(throwable: Throwable) {}
    protected open fun B.onSingleEvent(data: EmaExtraData) {}

}