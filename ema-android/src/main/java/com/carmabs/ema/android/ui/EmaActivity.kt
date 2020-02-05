package com.carmabs.ema.android.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.carmabs.ema.android.extra.EmaActivityResult
import com.carmabs.ema.android.extra.EmaReceiverModel
import com.carmabs.ema.android.extra.EmaResultModel
import com.carmabs.ema.android.viewmodel.EmaFactory
import com.carmabs.ema.android.viewmodel.EmaViewModel
import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.ema.core.state.EmaBaseState
import com.carmabs.ema.core.state.EmaState

/**
 *
 * Base activity to bind and unbind view model
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

abstract class EmaActivity<S : EmaBaseState, VM : EmaViewModel<S, NS>, NS : EmaNavigationState> : EmaToolbarFragmentActivity(), EmaView<S, VM, NS> {

    companion object {
        const val RESULT_DEFAULT_CODE: Int = 57535
    }

    /**
     * The view model of the fragment
     */
    private lateinit var vm: VM

    /**
     * The key id for incoming data through Bundle in activity instantiation.This is set up when other fragment/activity
     * launches a fragment with arguments provided by Bundle
     */
    protected open val inputStateKey: String by lazy {
        vm.initialViewState.javaClass.name
    }


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
    override fun onResume() {
        super.onResume()
        initializeViewModel(this)
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
     * Methods called when view model has been created
     * @param viewModel
     */
    final override fun onViewModelInitialized(viewModel: VM) {
        vm = viewModel
        onInitialized(viewModel)
    }

    /**
     * Called once the view model is instantiated
     * @param viewModel instantiated
     */
    abstract fun onInitialized(viewModel: VM)

    /**
     * The map which handles the view model attached with their respective scopes, to unbind the observers
     * when the view activity is destroyed
     */
    private val extraViewModelMap: MutableList<EmaViewModel<*, *>> by lazy { mutableListOf<EmaViewModel<*, *>>() }

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
            fragment: Fragment? = null,
            observerFunction: ((attachedState: EmaState<AS>) -> Unit)? = null): VM {

        val viewModel =
                fragment?.let {
                    ViewModelProviders.of(it, EmaFactory(viewModelAttachedSeed))[viewModelAttachedSeed::class.java]
                }
                        ?: ViewModelProviders.of(this, EmaFactory(viewModelAttachedSeed))[viewModelAttachedSeed::class.java]

        observerFunction?.also { viewModel.getObservableState().observe(this, Observer(it)) }
        extraViewModelMap.add(viewModel)

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
    override fun onPause() {
        super.onPause()
        removeExtraViewModels()
        vm.unBindObservables(this)
        vm.resultViewModel.unBindObservables(this)
    }

    /**
     * Remove extra view models attached
     */
    private fun removeExtraViewModels() {
        extraViewModelMap.forEach {
            it.unBindObservables(this)
        }
        extraViewModelMap.clear()
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
        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentManagerCycleCallbacks, false)
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
        }
    }


    /**
     * Use this method to add a activity result handler when a result is received from another activity
     */
    protected fun addOnActivityResultHandler(requestCode: Int, function: (Int, Int, Intent?) -> Unit) {
        resultActivityFunctions[requestCode] = function
    }

    override fun onDestroy() {
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentManagerCycleCallbacks)
        super.onDestroy()
    }
}