package com.carmabs.ema.android.base

import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.state.EmaExtraData

/**
 * View model to handle view states.
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
abstract class EmaViewModel<S,NS : EmaNavigationState> : EmaBaseViewModel<EmaState, NS>() {

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
        super.updateView(EmaState.Normal(state))
    }

    /**
     * Used for trigger an error on the view
     * Use the EmaState -> Error
     * @param error generated
     */
    protected fun notifyError(error: Throwable) {
        super.updateView(EmaState.Error(error))
    }

    /**
     * Used for trigger a loading event on the view
     * Use the EmaState -> Loading
     * @param data with loading information
     */
    protected fun loading(data: EmaExtraData?=null) {
        val loadingData = data?.let {
            EmaState.Loading(data = it)
        }?: EmaState.Loading()

        super.updateView(loadingData)
    }

    /**
     * Generate the initial state with EmaState to trigger normal/loading/error states
     * for the view.
     */
     override fun createInitialState(): EmaState {
        if (viewState == null) {
            viewState = createInitialViewState()
        }

        return EmaState.Normal(viewState)
    }

    /**
     * Generate the initial state of the view
     */
    abstract fun createInitialViewState(): S
}