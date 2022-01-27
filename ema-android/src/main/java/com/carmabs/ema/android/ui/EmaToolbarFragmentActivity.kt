package com.carmabs.ema.android.ui

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import androidx.annotation.CallSuper
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.viewbinding.ViewBinding
import com.carmabs.ema.android.R
import com.carmabs.ema.android.delegates.emaViewModelDelegate
import com.carmabs.ema.android.extension.checkVisibility
import com.carmabs.ema.android.viewmodel.EmaAndroidViewModel
import com.carmabs.ema.android.viewmodel.EmaFactory
import com.carmabs.ema.core.constants.FLOAT_ONE
import com.carmabs.ema.core.constants.FLOAT_ZERO
import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.ema.core.state.EmaBaseState
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.view.EmaView
import com.carmabs.ema.core.view.EmaViewModelTrigger
import com.carmabs.ema.core.viewmodel.EmaViewModel
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * [EmaFragmentActivity]  with toolbar support
 */
abstract class EmaToolbarFragmentActivity<B : ViewBinding, S : EmaBaseState, VM : EmaViewModel<S, NS>, NS : EmaNavigationState> :
    EmaFragmentActivity<B>(), EmaAndroidView<S, VM, NS> {

    final override val androidViewModelSeed: EmaAndroidViewModel<VM> by lazy {
        provideAndroidViewModel()
    }

    abstract fun provideAndroidViewModel(): EmaAndroidViewModel<VM>

    companion object {
        const val RESULT_DEFAULT_CODE: Int = 57535
    }

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
        mutableListOf<Job>()
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
     * The key id for incoming data through Bundle in activity instantiation.This is set up when other fragment/activity
     * launches a fragment with arguments provided by Bundle
     */
    protected open val inputStateKey: String = EmaView.KEY_INPUT_STATE_DEFAULT

    /**
     * Automatically updates previousState
     */
    override val updatePreviousStateAutomatically: Boolean = true

    /**
     * The incoming state in fragment instantiation. This is set up when other fragment/activity
     * launches a fragment with arguments provided by Bundle
     */
    override val inputState: S? by lazy { getInState() }

    /**
     * The toobar for the activity
     */
    protected lateinit var toolbar: Toolbar
        private set

    /**
     * The toolbar container for the activity
     */
    protected lateinit var toolbarLayout: AppBarLayout


    /**
     * Title for toolbar. If it is null the label xml tag in navigation layout is set for the toolbar
     * title, otherwise this title will be set for all fragments inside this activity.
     */
    abstract fun provideFixedToolbarTitle(): String?

    /**
     * Set true if activity use a custom theme to avoid the EmaTheme_NoActionBar theme set up
     */
    protected open val overrideTheme = false

    /**
     * Setup the toolbar
     * @param savedInstanceState for activity recreation
     */
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        //To enable support action bar
        if (!overrideTheme) setTheme(R.style.EmaTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setupToolbar()
    }

    /**
     * Initialize ViewModel on activity creation
     */
    @CallSuper
    override fun onStart() {
        onStartView(vm)
        viewJob = onBindView(this.lifecycleScope, vm)
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
     * Find the toolbar and its container for the activity. The toolbar must have the
     * id=@+id/emaToolbar. The toolbar contaienr [AppBarLayout] must have the id=@+ìd/emaAppBarLayout
     */
    private fun setupToolbar() {

        val tbToolbar = findViewById<Toolbar>(R.id.emaToolbar)
            ?: throw IllegalArgumentException("You must provide in your activity xml a Toolbar with android:id=@+id/emaToolbar")
        val lToolbar = findViewById<AppBarLayout>(R.id.emaAppBarLayout)
            ?: throw IllegalArgumentException("You must provide in your activity xml an AppBarLayout with android:id=@+ìd/emaAppBarLayout")

        setSupportActionBar(tbToolbar)
        toolbarLayout = lToolbar
        toolbar = tbToolbar
        setupActionBarWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            setToolbarTitle(provideFixedToolbarTitle())
        }
    }

    protected fun setToolbarTitle(title: String?) {
        supportActionBar?.title =
            title ?: provideFixedToolbarTitle() ?: navController.currentDestination?.label
    }

    /**
     * Hides the toolbar
     */
    protected open fun hideToolbar(gone: Boolean = true, animate: Boolean = true) {
        if (animate) {
            toolbarLayout.animate().apply {
                alpha(FLOAT_ZERO)
                setListener(object : Animator.AnimatorListener {

                    override fun onAnimationEnd(p0: Animator?) {
                        toolbarLayout.visibility = checkVisibility(false, gone)
                    }

                    override fun onAnimationStart(p0: Animator?) {

                    }

                    override fun onAnimationRepeat(p0: Animator?) {

                    }

                    override fun onAnimationCancel(p0: Animator?) {

                    }

                })
            }
        } else {
            toolbarLayout.visibility = if (gone) View.GONE else View.INVISIBLE
        }
    }

    /**
     * Show the toolbar
     */
    protected open fun showToolbar(animate: Boolean = true) {
        if(animate) {
            toolbarLayout.animate().apply {
                alpha(FLOAT_ONE)
                setListener(object : Animator.AnimatorListener {

                    override fun onAnimationStart(p0: Animator?) {
                        toolbarLayout.visibility = View.VISIBLE
                    }

                    override fun onAnimationEnd(p0: Animator?) {

                    }

                    override fun onAnimationCancel(p0: Animator?) {

                    }

                    override fun onAnimationRepeat(p0: Animator?) {

                    }
                })
            }
        }
        else{
            toolbarLayout.visibility = View.VISIBLE
        }
    }

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
                    EmaFactory(viewModelAttachedSeed)
                )[viewModelAttachedSeed::class.java]
            }
                ?: ViewModelProvider(
                    this,
                    EmaFactory(viewModelAttachedSeed)
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
     * Get the incoming state from another activity by the key [inputStateKey] provided
     */
    private fun getInState(): S? {
        return intent?.let {
            it.extras?.getSerializable(inputStateKey) as? S

        }
    }

    fun setInputState(inState: S) {
        intent = Intent().apply { putExtra(inputStateKey, inState) }
    }

    /**
     * Destroy the activity and unbind the observers from view model
     */
    override fun onStop() {
        removeExtraViewModels()
        onUnbindView(viewJob)
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

    final override fun onEmaStateError(error: Throwable) {
        binding.onStateError(error)
        isFirstErrorExecution = false
    }

    abstract fun B.onStateNormal(data: S)
    protected open fun B.onStateOverlayed(data: EmaExtraData) {}
    protected open fun B.onStateError(throwable: Throwable) {}
    protected open fun B.onSingleEvent(data: EmaExtraData) {}
}