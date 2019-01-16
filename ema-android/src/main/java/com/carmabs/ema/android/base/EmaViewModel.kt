package com.carmabs.ema.android.base

import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.state.EmaExtraData
import java.lang.Exception
import java.lang.RuntimeException

/**
 * View model to handle view states.
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
abstract class EmaViewModel<S,NS : EmaNavigationState> : EmaBaseViewModel<EmaState<S>, NS>() {

    /**
     * State of the view
     */
    protected var viewState: S? = null

    /**
     * Used for trigger an update on the view
     * Use the EmaState -> Normal
     * @param state of the view
     */
    protected fun updateViewState(state:S?) {
        state?.let {
            super.updateView(EmaState.Normal(state))
        }
    }

    /**
     * Used for trigger an error on the view
     * Use the EmaState -> Error
     * @param error generated
     */
    protected fun notifyError(error: Throwable) {
        viewState?.let {
            super.updateView(EmaState.Error(it,error))
        }?:throwInitialStateException()

    }

    /**
     * Used for trigger a loading event on the view
     * Use the EmaState -> Loading
     * @param data with loading information
     */
    protected fun loading(data: EmaExtraData?=null) {
        viewState?.let {state ->
            val loadingData = data?.let {
                EmaState.Loading(state,dataLoading = it)
            }?: EmaState.Loading(state)

            super.updateView(loadingData)
        }?:throwInitialStateException()

    }

    /**
     * Generate the initial state with EmaState to trigger normal/loading/error states
     * for the view.
     */
    override fun createInitialState(): EmaState<S> {
        if (viewState == null) {
            viewState = createInitialViewState()
        }

        return EmaState.Normal(viewState!!)
    }

    /**
     * Throws exception if the state of the view has not been initialized
     */
    private fun throwInitialStateException():Exception{
        throw RuntimeException("Initial state has not been created")
    }

    /**
     * Generate the initial state of the view
     */
    abstract fun createInitialViewState(): S
}