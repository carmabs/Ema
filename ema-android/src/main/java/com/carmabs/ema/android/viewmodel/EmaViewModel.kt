package com.carmabs.ema.android.viewmodel

import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.core.state.EmaState

/**
 * View model to handle view states.
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
abstract class EmaViewModel<S, NS : EmaNavigationState> : EmaBaseViewModel<EmaState<S>, NS>() {

    /**
     * State of the view
     */
    protected var viewState: S? = null


    override fun onStart(inputState: EmaState<S>?): Boolean {
        if (viewState == null)
            inputState?.let { viewState = it.data }
        return super.onStart(inputState)
    }

    /**
     * Update the current state and update the view by default
     * @param notifyView updates the view
     * @param changeStateFunction create the new state
     */
    protected fun updateViewState(notifyView: Boolean = true, changeStateFunction: S.() -> S) {
        viewState?.let {
            viewState = changeStateFunction.invoke(it)
            viewState?.let { newState -> state = EmaState.Normal(newState) }

            if (notifyView) updateViewState()
        }

    }

    /**
     * Used for trigger an update on the view
     * Use the EmaState -> Normal
     * @param state of the view
     */
    protected fun updateViewState() {
        state?.let {
            viewState?.let { currentState ->
                super.updateView(EmaState.Normal(currentState))
            }
        }
    }

    /**
     * Check the current view state
     * @param checkStateFunction function to check the current state
     */
    protected fun checkViewState(checkStateFunction: (S) -> Unit){
        viewState?.run{
            checkStateFunction.invoke(this)
        }
    }

    /**
     * Used for trigger an error on the view
     * Use the EmaState -> Error
     * @param error generated
     */
    protected fun notifyError(error: Throwable) {
        viewState?.let {
            super.updateView(EmaState.Error(it, error))
        } ?: throwInitialStateException()

    }

    /**
     * Used for trigger a loading event on the view
     * Use the EmaState -> Loading
     * @param data with loading information
     */
    protected fun loading(data: EmaExtraData? = null) {
        viewState?.let { state ->
            val loadingData = data?.let {
                EmaState.Loading(state, dataLoading = it)
            } ?: EmaState.Loading(state)

            super.updateView(loadingData)
        } ?: throwInitialStateException()

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
    private fun throwInitialStateException(): Exception {
        throw RuntimeException("Initial state has not been created")
    }

    /**
     * Generate the initial state of the view
     */
    abstract fun createInitialViewState(): S
}