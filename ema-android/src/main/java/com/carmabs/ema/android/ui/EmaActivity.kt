package com.carmabs.ema.android.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.AnimRes
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.carmabs.ema.android.delegates.emaViewModelDelegate
import com.carmabs.ema.android.extension.addOnBackPressedListener
import com.carmabs.ema.android.navigation.EmaActivityNavControllerNavigator
import com.carmabs.ema.android.navigation.EmaNavControllerNavigator
import com.carmabs.ema.android.viewmodel.EmaAndroidViewModel
import com.carmabs.ema.android.viewmodel.EmaViewModelFactory
import com.carmabs.ema.core.initializer.EmaInitializer
import com.carmabs.ema.core.navigator.EmaDestination
import com.carmabs.ema.core.state.EmaDataState
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.view.EmaViewModelTrigger
import com.carmabs.ema.core.viewmodel.EmaViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.core.scope.Scope

/**
 *
 * Abstract base class to implement Kodein framework in activity context
 * to handle dependency injection
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
abstract class EmaActivity<B : ViewBinding, S : EmaDataState, VM : EmaViewModel<S, D>, D : EmaDestination> :
    AppCompatActivity(), EmaAndroidView<S, VM, D>, AndroidScopeComponent {

    protected lateinit var binding: B

    final override val scope: Scope by activityScope()

    /**
     * Method to provide the activity ViewBinding class to represent the layout.
     */
    abstract fun createViewBinding(inflater: LayoutInflater): B


    /**
     * The onCreate base will set the view specified in [.getLayout] and will
     * inject dependencies and views.
     *
     */
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Call scope to call scope to enable injection
        scope
        binding = createViewBinding(layoutInflater)
        setContentView(binding.root)
        vm.onBackHardwarePressedListener?.also {
            addOnBackPressedListener(it)
        }
        (navigator as? EmaActivityNavControllerNavigator)?.setup(overrideDestinationInitializer())

    }

    /**
     * Set up the up action navigation
     */
    override fun onSupportNavigateUp() =
        (navigator as? EmaNavControllerNavigator)?.navController?.navigateUp() ?: false

    protected open fun overrideDestinationInitializer(): EmaInitializer? = null


    final override val androidViewModelSeed: EmaAndroidViewModel<VM> by lazy {
        provideAndroidViewModel()
    }

    abstract fun provideAndroidViewModel(): EmaAndroidViewModel<VM>

    override val coroutineScope: CoroutineScope
        get() = lifecycleScope


    override val viewModelSeed: VM
        get() = androidViewModelSeed.emaViewModel

    /**
     * Determines first execution for each one of the state methods. EmaView determines when to set it to false.
     */
    protected var isFirstNormalExecution: Boolean = true
        private set

    protected var isFirstOverlayedExecution: Boolean = true
        private set

    protected var isFirstErrorExecution: Boolean = true
        private set

    private var viewJob: MutableList<Job>? = null

    /**
     * The map which handles the view model attached with their respective scopes, to unbind the observers
     * when the view activity is destroyed
     */
    private val extraViewModelList: MutableList<EmaAndroidViewModel<VM>> by lazy { mutableListOf() }

    private val extraViewJobs: MutableList<Job> by lazy {
        mutableListOf()
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
     * The incoming initializer in activity instantiation. This is set up when other fragment/activity
     * launches an activity with arguments provided by Bundle
     */
    override val initializer: EmaInitializer? by lazy { getInitializerIntent() }


    /**
     * Initialize ViewModel on activity creation
     */
    @CallSuper
    override fun onStart() {
        viewJob = onBindView(this.lifecycleScope, vm)
        onStartView(vm)
        super.onStart()
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
     * Previous state for comparing state properties update
     */
    override var previousState: S? = null


    /**
     * Add a view model observer to current fragment
     * @param viewModelAttachedSeed is the view model seed will used as factory instance if there is no previous
     * view model retained by the OS
     * @param fragment the fragment scope
     * @param observerFunction the observer of the view model attached
     * @return The view model attached
     */
    fun <AVM : EmaAndroidViewModel<out EmaViewModel<*, *>>> addExtraViewModel(
        viewModelAttachedSeed: AVM,
        fragment: Fragment? = null,
        observerFunction: ((attachedState: EmaState<*>) -> Unit)? = null
    ): AVM {

        val viewModel =
            fragment?.let {
                ViewModelProvider(
                    it,
                    EmaViewModelFactory(viewModelAttachedSeed)
                )[viewModelAttachedSeed::class.java]
            }
                ?: ViewModelProvider(
                    this,
                    EmaViewModelFactory(viewModelAttachedSeed)
                )[viewModelAttachedSeed::class.java]

        observerFunction?.also {
            extraViewJobs.add(coroutineScope.launch {
                viewModel.emaViewModel.getObservableState().collect {
                    observerFunction.invoke(it)
                }
            }
            )
        }
        extraViewModelList.add(viewModel as EmaAndroidViewModel<VM>)

        return viewModel
    }

    /**
     * Get the incoming state from another activity by the key [initializer] provided
     */
    private fun getInitializerIntent(): EmaInitializer? {
        return intent?.let {
            it.extras?.getSerializable(EmaInitializer.KEY) as? EmaInitializer

        }
    }

    fun setInitializer(initializer: EmaInitializer) {
        intent = Intent().apply { putExtra(EmaInitializer.KEY, initializer) }
    }

    /**
     * Destroy the activity and unbind the observers from view model
     */
    @CallSuper
    override fun onStop() {
        removeExtraViewModels()
        onUnbindView(viewJob, vm)
        super.onStop()
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

    final override fun onEmaStateNormal(data: S) {
        binding.onStateNormal(data)
        isFirstNormalExecution = false
    }

    final override fun onEmaStateOverlayed(data: EmaExtraData) {
        binding.onStateOverlayed(data)
        isFirstOverlayedExecution = false
    }

    final override fun onSingleEvent(data: EmaExtraData) {
        binding.onSingleEvent(data)
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        scope.close()
    }

    final override fun onEmaStateErrorOverlayed(error: Throwable) {
        binding.onStateErrorOverlayed(error)
        isFirstErrorExecution = false
    }

    @CallSuper
    override fun finish() {
        super.finish()
        overridePopTransitionAnimations()?.also {
            overridePendingTransition(it.enterTransition,it.exitTransition)
        }
    }

    final override fun onBack(): Boolean {
        finish()
        return false
    }

    //Override it to make it final to avoid deprecated implementation
    final override fun onBackPressed() {
        super.onBackPressed()
    }

    protected open fun overridePopTransitionAnimations():EmaPopActivityTransitionAnimations? = null

    abstract fun B.onStateNormal(data: S)
    protected open fun B.onStateOverlayed(data: EmaExtraData) {}
    protected open fun B.onStateErrorOverlayed(throwable: Throwable) {}
    protected open fun B.onSingleEvent(data: EmaExtraData) {}

    protected data class EmaPopActivityTransitionAnimations(
        @AnimRes val enterTransition: Int,
        @AnimRes val exitTransition: Int
    )
}