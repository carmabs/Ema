package com.carmabs.ema.android.base

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.annotation.AnimRes
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.carmabs.ema.android.delegates.emaViewModelDelegate
import com.carmabs.ema.android.extension.addOnBackPressedListener
import com.carmabs.ema.android.extension.getSerializableCompat
import com.carmabs.ema.android.navigation.EmaActivityNavControllerNavigator
import com.carmabs.ema.android.navigation.EmaNavControllerNavigator
import com.carmabs.ema.android.ui.EmaAndroidView
import com.carmabs.ema.android.viewmodel.EmaAndroidViewModel
import com.carmabs.ema.android.viewmodel.EmaViewModelFactory
import com.carmabs.ema.core.constants.INT_ZERO
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
abstract class EmaCoreActivity<S : EmaDataState, VM : EmaViewModel<S, D>, D : EmaDestination> :
    AppCompatActivity(), EmaAndroidView<S, VM, D>, AndroidScopeComponent {

    final override val scope: Scope by activityScope()


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
        vm.onBackHardwarePressedListener?.also {
            addOnBackPressedListener(it)
        }
    }

    @CallSuper
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        (navigator as? EmaActivityNavControllerNavigator)?.setup(overrideDestinationInitializer())
    }

    /**
     * Set up the up action navigation
     */
    override fun onSupportNavigateUp() =
        (navigator as? EmaNavControllerNavigator)?.navController?.navigateUp() ?: false

    protected open fun overrideDestinationInitializer(): EmaInitializer? = null


    final override val androidViewModelSeed: EmaAndroidViewModel by lazy {
        provideAndroidViewModel()
    }

    abstract fun provideAndroidViewModel(): EmaAndroidViewModel

    override val coroutineScope: CoroutineScope
        get() = lifecycleScope


    override val viewModelSeed: VM
        get() = androidViewModelSeed.emaViewModel as VM


    private var viewJob: MutableList<Job>? = null

    /**
     * The map which handles the view model attached with their respective scopes, to unbind the observers
     * when the view activity is destroyed
     */
    private val extraViewModelList: MutableList<EmaAndroidViewModel> by lazy { mutableListOf() }

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
    final override var previousEmaState: EmaState<S>? = null


    /**
     * Add a view model observer to current fragment
     * @param viewModelAttachedSeed is the view model seed will used as factory instance if there is no previous
     * view model retained by the OS
     * @param fragment the fragment scope
     * @param observerFunction the observer of the view model attached
     * @return The view model attached
     */
    fun <AVM : EmaAndroidViewModel> addExtraViewModel(
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
        extraViewModelList.add(viewModel as EmaAndroidViewModel)

        return viewModel
    }

    /**
     * Get the incoming state from another activity by the key [initializer] provided
     */
    private fun getInitializerIntent(): EmaInitializer? {
        return intent?.let {
            it.extras?.getSerializableCompat(EmaInitializer.KEY)

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


    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        scope.close()
    }

    @CallSuper
    override fun finish() {
        super.finish()
        overridePopTransitionAnimations()?.also {
            overridePendingTransition(it.enterTransition ?: INT_ZERO, it.exitTransition ?: INT_ZERO)
        }
    }

    override fun onEmaStateOverlayed(data: EmaExtraData) = Unit

    override fun onSingleEvent(data: EmaExtraData) = Unit

    final override fun onBack(): Boolean {
        finish()
        return false
    }

    //Override it to make it final to avoid deprecated implementation
    @Suppress("DEPRECATION")
    @Deprecated(
        "Overrides deprecated member in 'androidx.core.app.ComponentActivity'. Deprecated in Java",
        ReplaceWith("super.onBackPressed()", "androidx.appcompat.app.AppCompatActivity")
    )
    final override fun onBackPressed() {
        super.onBackPressed()
    }

    protected open fun overridePopTransitionAnimations(): EmaPopActivityTransitionAnimations? = null


    protected data class EmaPopActivityTransitionAnimations(
        @AnimRes val enterTransition: Int?,
        @AnimRes val exitTransition: Int?
    )
}