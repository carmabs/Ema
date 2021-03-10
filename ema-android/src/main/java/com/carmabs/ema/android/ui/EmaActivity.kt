package com.carmabs.ema.android.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.carmabs.ema.android.delegates.emaViewModelDelegate
import com.carmabs.ema.android.extra.EmaActivityResult
import com.carmabs.ema.android.viewmodel.EmaAndroidViewModel
import com.carmabs.ema.android.viewmodel.EmaFactory
import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.ema.core.state.EmaBaseState
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.view.EmaViewModelTrigger
import com.carmabs.ema.core.viewmodel.EmaReceiverModel
import com.carmabs.ema.core.viewmodel.EmaResultModel
import com.carmabs.ema.core.viewmodel.EmaViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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
     * Activity result model. It if checked to notify result to viewmodels.
     */
    private var activityResult: EmaActivityResult? = null

    /**
     * Activity result function to execute the viewmodel function when activity result is received.
     */
    private val resultActivityFunctions: HashMap<Int, ((Int, Int, Intent?) -> Unit)> by lazy {
        hashMapOf<Int, ((Int, Int, Intent?) -> Unit)>()
    }

    /**
     * Fragment lifeCycle callbacks to notify results when fragment is added, this guarantee a fragment receiver
     * has preference if the container activity has the same receiver, only one will be executed.
     *
     * Notify the results after fragments are created and viewmodels initalized
     */
    private val fragmentManagerCycleCallbacks: FragmentManager.FragmentLifecycleCallbacks by lazy {
        object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                super.onFragmentResumed(fm, f)
                notifyResults()
            }
        }
    }

    /**
     * Initialize ViewModel on activity creation
     */
    override fun onStart() {
        super.onStart()
        runBlocking {
            viewJob = onStartAndBindData(
                this@EmaActivity,
                vm,
                vm.resultViewModel
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
     * Method use to notify the results of another activities
     */
    private fun notifyResults() {

        //Check the activity result to launch the result function. It is implemented here
        //because onResume is executed after onActivityResult and here the view model is already
        //initialized, otherwise the function wouldn't be executed on viewmodel

        activityResult?.apply {
            resultActivityFunctions.forEach {
                if (it.key == requestCode)
                    it.value.invoke(requestCode, requestCode, data)
            }

            resultActivityFunctions.remove(requestCode)
        }
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

    /**
     * Called everytime a result is setted on viewmodel
     */
    override fun onResultSetEvent(emaResultModel: EmaResultModel) {
        setResult(parseResult(emaResultModel.resultState), intent.apply {
            putExtra(emaResultModel.id.toString(), emaResultModel)
        })
    }

    /**
     * Called everytime a receiver is invoked on viewmodel
     */
    override fun onResultReceiverInvokeEvent(emaReceiverModel: EmaReceiverModel) {
        intent.removeExtra(emaReceiverModel.resultId.toString())
    }


    override fun onCreateActivity(savedInstanceState: Bundle?) {
        super.onCreateActivity(savedInstanceState)
        supportFragmentManager.registerFragmentLifecycleCallbacks(
            fragmentManagerCycleCallbacks,
            false
        )
    }

    final override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        activityResult = EmaActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RESULT_DEFAULT_CODE -> {

                resultActivityFunctions[requestCode] = { _, _, _ ->
                    data?.extras?.apply {
                        keySet()?.forEach { idResult ->
                            getSerializable(idResult)?.also {
                                val resultModel = it as EmaResultModel
                                vm.apply {
                                    resultViewModel.addResult(resultModel)
                                    resultViewModel.notifyResults(resultModel.ownerId)
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    private fun parseResult(emaResultModel: EmaResultModel.Result): Int {
        return when (emaResultModel) {
            is EmaResultModel.Result.Success -> Activity.RESULT_OK
            is EmaResultModel.Result.Fail -> Activity.RESULT_CANCELED
            is EmaResultModel.Result.Other -> Activity.RESULT_OK
            else -> Activity.RESULT_CANCELED
        }
    }


    /**
     * Use this method to add a activity result handler when a result is received from another activity
     */
    protected fun addOnActivityResultHandler(
        requestCode: Int,
        function: (Int, Int, Intent?) -> Unit
    ) {
        resultActivityFunctions[requestCode] = function
    }

    override fun onDestroy() {
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentManagerCycleCallbacks)
        super.onDestroy()
    }
}