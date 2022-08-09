package com.carmabs.ema.android.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.carmabs.ema.android.delegates.emaViewModelDelegate
import com.carmabs.ema.android.di.Injector
import com.carmabs.ema.android.navigation.EmaFragmentNavControllerNavigator
import com.carmabs.ema.android.viewmodel.EmaAndroidViewModel
import com.carmabs.ema.android.viewmodel.EmaFactory
import com.carmabs.ema.core.initializer.EmaInitializer
import com.carmabs.ema.core.navigator.EmaNavigationTarget
import com.carmabs.ema.core.state.EmaBaseState
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.view.EmaView
import com.carmabs.ema.core.view.EmaViewModelTrigger
import com.carmabs.ema.core.viewmodel.EmaViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.android.x.closestDI


/**
 *
 * Abstract base class to implement Kodein framework in fragment context
 * to handle dependency injection
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
abstract class EmaBaseFragment<S : EmaBaseState, VM : EmaViewModel<S, NT>, NT : EmaNavigationTarget> :
Fragment(), EmaAndroidView<S, VM, NT>, Injector {

    protected var isFirstNormalExecution: Boolean = true
        private set

    protected var isFirstOverlayedExecution: Boolean = true
        private set

    protected var isFirstErrorExecution: Boolean = true
    private set

    abstract override val navigator: EmaFragmentNavControllerNavigator<NT>?

    final override val androidViewModelSeed: EmaAndroidViewModel<VM> by lazy {
        provideAndroidViewModel()
    }

    abstract fun provideAndroidViewModel(): EmaAndroidViewModel<VM>

    final override val parentKodein: DI by closestDI()


    final override val di: DI by lazy {
        injectKodein()
    }

    final override fun injectModule(kodeinBuilder: DI.MainBuilder): DI.Module? =
        injectFragmentModule(kodeinBuilder)


    abstract fun injectFragmentModule(kodein: DI.MainBuilder): DI.Module?


    final override val viewModelSeed: VM
    get() = androidViewModelSeed.emaViewModel

    private val extraViewJobs: MutableList<Job> by lazy {
        mutableListOf()
    }

    override val coroutineScope: CoroutineScope
    get() = lifecycleScope

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isFirstNormalExecution = true
        isFirstOverlayedExecution = true
        isFirstErrorExecution = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

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


    /**
     * The view model is instantiated on fragment resume.
     */
    override fun onStart() {
        super.onStart()
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
        removeExtraViewModels()
        super.onStop()
        Log.d("NAV", "ONSTOP")
    }

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
    override fun onNavigation(navigation: NT?) {
        super.onNavigation(navigation)
    }

    fun setInitializer(initializer: EmaInitializer) {
        arguments = Bundle().apply { putSerializable(EmaInitializer.KEY, initializer) }
    }

    override fun onEmaStateNormal(data: S) {
        onStateNormal(data)
        isFirstNormalExecution = false
    }

    override fun onEmaStateOverlayed(data: EmaExtraData) {
        onStateOverlayed(data)
        isFirstOverlayedExecution = false
    }

    override fun onEmaStateError(error: Throwable) {
        onStateError(error)
        isFirstErrorExecution = false
    }

    @CallSuper
    override fun onDestroyView() {
        previousState = null
        super.onDestroyView()
    }

    abstract fun onStateNormal(data: S)

    abstract fun onStateOverlayed(data: EmaExtraData)

    abstract fun onStateError(error: Throwable)
}