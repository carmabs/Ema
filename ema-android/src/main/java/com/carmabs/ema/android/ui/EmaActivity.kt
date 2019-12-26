package com.carmabs.ema.android.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
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
        const val SAVE_INSTANCE_RECEIVER_ID = "SAVE_INSTANCE_RECEIVER_ID"
    }

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
     * The incoming state in fragment instantiation. This is set up when other fragment/activity
     * launches a fragment with arguments provided by Bundle
     */
    override val inputState: S? by lazy { getInState() }


    private val receiverId: HashMap<Int, Unit> by lazy {
        hashMapOf<Int, Unit>()
    }

    private var activityResult: EmaActivityResult? = null

    private val resultActivityFunctions: HashMap<Int, ((Int, Int, Intent?) -> Unit)> by lazy {
        hashMapOf<Int, ((Int, Int, Intent?) -> Unit)>()
    }

    /**
     * Initialize ViewModel on activity creation
     */
    override fun onResume() {
        super.onResume()
        initializeViewModel(this)
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
    override fun onViewModelInitalized(viewModel: VM) {
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
        vm?.unBindObservables(this)
        vm?.resultViewModel?.unBindObservables(this)
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

    override fun onResult(emaResultModel: EmaResultModel) {
        setResult(parseResult(emaResultModel.resultState), intent.apply {
            putExtra(emaResultModel.id.toString(), emaResultModel.data)
        })
    }

    override fun onReceiverAdded(allReceivers: HashMap<Int, EmaReceiverModel>) {
        allReceivers.keys.forEach {
            receiverId[it] = Unit
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        savedInstanceState?.getSerializable("P")?.also { intArray ->
            (intArray as? IntArray)?.forEach {
                receiverId[it] = Unit
            }
        }
        super.onCreate(savedInstanceState)

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("P", receiverId.keys.toIntArray())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        activityResult = EmaActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RESULT_DEFAULT_CODE -> {
                resultActivityFunctions[requestCode] = { _, _, dataIntent ->
                    receiverId.keys.forEach { codeReceiver ->
                        dataIntent?.getSerializableExtra(codeReceiver.toString())?.also {
                            vm?.resultViewModel?.notifyResult(
                                    ownerCode = getOwnerId(),
                                    emaResultModel = EmaResultModel(codeReceiver, it)
                            )
                        }
                    }

                }
            }
        }
    }

    private fun parseResult(emaResultModel: EmaResultModel.Result): Int {
        return when (emaResultModel) {
            EmaResultModel.Result.Success -> Activity.RESULT_OK
            EmaResultModel.Result.Fail -> Activity.RESULT_CANCELED
            is EmaResultModel.Result.Other -> Activity.RESULT_OK
        }
    }

    private fun getOwnerId(): Int {
        return this.javaClass.name.hashCode()
    }

    protected fun addOnResultActivityHandler(requestCode:Int,function:(Int,Int,Intent?)->Unit) {
        resultActivityFunctions[requestCode]=function
    }
}