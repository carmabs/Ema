package com.carmabs.ema.android.ui

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.carmabs.ema.android.delegates.emaViewModelDelegate
import com.carmabs.ema.android.viewmodel.EmaAndroidViewModel
import com.carmabs.ema.android.viewmodel.EmaFactory
import com.carmabs.ema.core.delegate.emaBooleanDelegate
import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.ema.core.state.EmaBaseState
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.view.EmaViewModelTrigger
import com.carmabs.ema.core.viewmodel.EmaResultHandler
import com.carmabs.ema.core.viewmodel.EmaViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.kodein.di.generic.instance

/**
 *
 * Base activity to bind and unbind view model
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

abstract class EmaActivity<S : EmaBaseState, VM : EmaViewModel<S, NS>, NS : EmaNavigationState> :
    EmaToolbarFragmentActivity(), EmaAndroidView<S, VM, NS> {

    companion object {
        const val RESULT_DEFAULT_CODE: Int = 57535
    }

    override val coroutineScope: CoroutineScope
        get() = lifecycleScope


    override val viewModelSeed: VM
        get() = androidViewModelSeed.emaViewModel

    private var viewJob: MutableList<Job>? = null

    /**
     * The map which handles the view model attached with their respective scopes, to unbind the observers
     * when the view activity is destroyed
     */
    private val extraViewModelList: MutableList<EmaAndroidViewModel<*>> by lazy { mutableListOf<EmaAndroidViewModel<*>>() }

    private val extraViewJobs: MutableList<Job> by lazy {
        mutableListOf<Job>()
    }

    /**
     * Determines first execution for each one of the state methods. EmaView determines when to set it to false.
     */
    final override var isFirstNormalExecution: Boolean by emaBooleanDelegate(true)

    final override var isFirstAlternativeExecution: Boolean by emaBooleanDelegate(true)

    final override var isFirstErrorExecution: Boolean by emaBooleanDelegate(true)

    /**
     * The view model of the fragment
     */
    protected val vm: VM by emaViewModelDelegate()

    /**
     * Trigger to start viewmodel only when startViewModel is launched
     */
    override val startTrigger: EmaViewModelTrigger?=null

    /**
     * The key id for incoming data through Bundle in activity instantiation.This is set up when other fragment/activity
     * launches a fragment with arguments provided by Bundle
     */
    protected open val inputStateKey: String by lazy {
        vm.initialViewState.javaClass.name
    }

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
     * Initialize ViewModel on activity creation
     */
    override fun onStart() {
        super.onStart()
        runBlocking {
            viewJob = onStartAndBindData(
                this@EmaActivity,
                vm
            )
        }
    }

    /**
     * Notifies the view model that view has been gone to foreground.
     */
    override fun onResume() {
        super.onResume()
        onResumeView(vm)
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
     * @param fragmentActivity the activity scope, if it is provided this will be the scope of the view model attached
     * @param observerFunction the observer of the view model attached
     * @return The view model attached
     */
    fun <S, VM : EmaAndroidViewModel<EmaViewModel<S,*>>> addExtraViewModel(
        viewModelAttachedSeed: VM,
        fragment: Fragment? = null,
        observerFunction: ((attachedState: EmaState<S>) -> Unit)? = null
    ): VM {

        val viewModel =
            fragment?.let {
                ViewModelProviders.of(
                    it,
                    EmaFactory(viewModelAttachedSeed)
                )[viewModelAttachedSeed::class.java]
            }
                ?: ViewModelProviders.of(
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
        extraViewModelList.add(viewModel)

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
        super.onStop()
        removeExtraViewModels()
        runBlocking {
            onStopBinding(viewJob)
        }
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
}