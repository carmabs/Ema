package com.carmabs.ema.core.viewmodel

import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.core.state.EmaState

/**
 * View model to handle view states.
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
abstract class EmaViewModel<S : Any, NS : EmaNavigationState> :
    EmaBaseViewModel<EmaState<S>, NS>() {

    /**
     * State of the view
     */
    private lateinit var viewState: S

    private val emaResultHandler: EmaResultHandler = EmaResultHandler.getInstance()

    override fun onStart(inputState: EmaState<S>?): Boolean {
        if (!this::viewState.isInitialized)
            inputState?.let { viewState = it.data }
        onResultListenerSetup()
        return super.onStart(inputState)
    }

    /**
     * Here should implement the listener for result data from other views through [addOnResultReceived] method
     */
    protected open fun onResultListenerSetup() {
        //Calls to [addOnResultReceived] if they are needed
    }


    /**
     * Update the data of the state without notifying it to the view.
     */
    private fun updateData(newState: S): EmaState<S> {
        return when (state) {
            is EmaState.Error -> {
                val errorState = state as EmaState.Error
                EmaState.Error(newState, errorState.error)
            }
            is EmaState.Normal -> {
                EmaState.Normal(newState)
            }

            is EmaState.Alternative -> {
                val alternativeState = state as EmaState.Alternative
                EmaState.Alternative(newState, alternativeState.dataAlternative)
            }
        }
    }

    /**
     * Update the current state and update the normal view state by default
     * @param notifyView updates the view
     * @param changeStateFunction create the new state
     */
    protected open fun updateToNormalState(changeStateFunction: S.() -> S) {
        viewState = changeStateFunction.invoke(viewState)
        state = EmaState.Normal(viewState)
        updateToNormalState()
    }

    /**
     * Used for trigger an update on the view
     * Use the EmaState -> Normal
     * @param state of the view
     */
    protected open fun updateToNormalState() {
        super.updateView(EmaState.Normal(viewState))
    }

    /**
     * Update the data of current state without notify it to the view.
     * @param changeStateFunction create the new state
     */
    protected open fun updateDataState(changeStateFunction: S.() -> S) {
        viewState = changeStateFunction.invoke(viewState)
        state = updateData(viewState)
    }


    /**
     * Get the current view state
     * @return the current viewState or null if it has not been initialized
     */
    fun getDataState(): S {
        return viewState
    }


    /**
     * Used for trigger a updateAlternativeState event on the view
     * Use the EmaState -> Alternative
     * @param data with updateAlternativeState information
     */
    protected open fun updateToAlternativeState(data: EmaExtraData? = null) {
        val alternativeData: EmaState.Alternative<S> = data?.let {
            EmaState.Alternative(viewState, dataAlternative = it)
        } ?: EmaState.Alternative(viewState)
        super.updateView(alternativeData)
    }

    /**
     * Generate the initial state with EmaState to trigger normal/updateAlternativeState/error states
     * for the view.
     */
    final override fun createInitialState(): EmaState<S> {
        if (!this::viewState.isInitialized) {
            viewState = initialViewState
        }

        return EmaState.Normal(viewState)
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
    abstract val initialViewState: S

    /**
     * Set a result for previous view when the current one is destroyed
     */
    protected fun addResult(data: Any, code: Int = EmaResultHandler.RESULT_ID_DEFAULT) {
        emaResultHandler.addResult(
            EmaResultModel(
                code = code,
                ownerId = getId(),
                data = data
            )
        )
    }

    /**
     * Set the listener for back data when the result view is destroyed
     */
    protected fun addOnResultReceived(
        code: Int = EmaResultHandler.RESULT_ID_DEFAULT,
        receiver: (EmaResultModel) -> Unit
    ) {
        val emaReceiver = EmaReceiverModel(
            ownerId = getId(),
            resultCode = code,
            function = receiver
        )
        emaResultHandler.addResultReceiver(emaReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        emaResultHandler.notifyResults(getId())
    }

    fun getId(): Int {
        return this.javaClass.name.hashCode()
    }
}