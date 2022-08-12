package com.carmabs.ema.core.viewmodel

import com.carmabs.ema.core.concurrency.ConcurrencyManager
import com.carmabs.ema.core.concurrency.DefaultConcurrencyManager
import com.carmabs.ema.core.concurrency.tryCatch
import com.carmabs.ema.core.constants.INT_ONE
import com.carmabs.ema.core.initializer.EmaInitializer
import com.carmabs.ema.core.navigator.EmaDestination
import com.carmabs.ema.core.state.EmaBaseState
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.core.state.EmaState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * View model to handle view states.
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
abstract class EmaViewModel<S : EmaBaseState, D : EmaDestination> {

    private val pendingEvents = mutableListOf<()->Unit>()
    /**
     * Observable state that launch event every time a value is set. This value will be the state
     * of the view. When the ViewModel is attached to an observer, if this value is already set up,
     * it will be notified to the new observer. Could be different from state if some changes of the
     * current state has not been notified to the view (Ex: a switch has been changed and the state has
     * been modified, but we don't want no notify to the view to avoid infinite loop ->
     *  switch modified
     *      -> switch state saved on view model if there is view recreation
     *          -> it is notified to the view
     *              -> switch has been set again
     *                  -> saved in view model ------> INFINITE LOOP)
     */
    private val observableState: MutableSharedFlow<EmaState<S>> = MutableSharedFlow(
        replay = INT_ONE,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * Observable state that launch event every time a value is set. This value will be a [EmaExtraData]
     * object that can contain any type of object. It will be used for
     * events that only has to be notified once to its observers, e.g: A toast message.
     */
    private val singleObservableState: MutableSharedFlow<EmaExtraData> = MutableSharedFlow(
        extraBufferCapacity = INT_ONE,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * Observable state that launch event every time a value is set. [NT] value be will a [EmaDestination]
     * object that represent the destination. This observable will be used for
     * events that only has to be notified once to its observers and is used to notify the navigation
     * events
     */
    private val navigationState: MutableSharedFlow<D?> = MutableSharedFlow(
        replay = INT_ONE,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * Manager to handle the threads where the background tasks are going to be launched
     */
    var concurrencyManager: ConcurrencyManager = DefaultConcurrencyManager()

    /**
     * The state of the view.
     */
    internal lateinit var state: EmaState<S>

    /**
     * Determine if viewmodel is first time resumed
     *
     */
    private var firstTimeResumed: Boolean = true

    /**
     * To determine if the view must be updated when view model is created automatically
     */
    protected open val updateOnInitialization: Boolean = true

    /**
     * Determine if the viewmodel has initialized its state
     */

    var hasBeenInitialized: Boolean = false
        private set

    /**
     * Methods called the first time ViewModel is created
     * @param initializer
     * @return true if it's the first time is started
     */
    internal fun onStart(initializer: EmaInitializer? = null) {
        if (!this::state.isInitialized) {
            concurrencyManager.launch {
                normalContentData = onCreateState(initializer)
                state = EmaState.Normal(normalContentData)
                hasBeenInitialized = true
                pendingEvents.forEach {
                    it.invoke()
                }
                onResultListenerSetup()
                if (updateOnInitialization)
                    observableState.tryEmit(state)
                onCreated()
                pendingEvents.clear()
            }
        } else {
            //We call this to update the data if it has been not be emitted
            // if last time was updated by updateDataState
            observableState.tryEmit(state)
        }
        onViewStarted()
    }

    /**
     * Called when view is created by first time, it means, it is added to the stack
     */
    abstract suspend fun onCreateState(initializer: EmaInitializer? = null): S

    protected open suspend fun onCreated() = Unit
    /**
     * Called when view is shown in foreground
     */
    internal fun onResumeView() {
        useAfterStateIsCreated {
            onViewResumed()
        }
        firstTimeResumed = false
    }

    /**
     * Called when view is hidden in background
     */
    internal fun onPauseView() {
        useAfterStateIsCreated {
            onViewPaused()
        }
    }

    internal fun onStopView(){
        useAfterStateIsCreated {
            onViewStopped()
        }
    }

    /**
     * Warranty the call is called only after state is created
     */
    private fun useAfterStateIsCreated(action:()->Unit){
        if(hasBeenInitialized)
            action()
        else
            pendingEvents.add(action)
    }

    /**
     * Called always the view goes to the foreground
     */
    protected open fun onViewResumed() = Unit

    /**
     * Called always the view is not fully visible
     */
    protected open fun onViewPaused() = Unit

    /**
     * Called when the view is visible to the user
     */
    protected open fun onViewStarted() = Unit

    /**
     * Called always the view goes to the background
     */
    protected open fun onViewStopped() = Unit


    /**
     * Get observable state as LiveDaya to avoid state setting from the view
     */
    fun getObservableState(): SharedFlow<EmaState<S>> = observableState

    /**
     * Get current state of view
     */
    fun getCurrentState(): EmaState<S> = state

    /**
     * Get navigation state as LiveData to avoid state setting from the view
     */
    fun getNavigationState(): SharedFlow<D?> = navigationState

    /**
     * Get single state as LiveData to avoid state setting from the view
     */
    fun getSingleObservableState(): SharedFlow<EmaExtraData> = singleObservableState


    /**
     * Method used to update the state of the view. It will be notified to the observers
     * @param state Tee current state of the view
     */
    private fun updateView(state: EmaState<S>) {
        this.state = state
        observableState.tryEmit(state)
    }

    /**
     * Method used to notify to the observer for a single event that will be notified only once time.
     * It a new observer is attached, it will not be notified
     */
    protected open fun notifySingleEvent(extraData: EmaExtraData) {
        singleObservableState.tryEmit(extraData)
    }

    /**
     * Method use to notify a navigation event
     * @param navigation The object that represent the destination of the navigation
     */
    protected open fun navigate(navigation: D) {
        navigation.resetNavigated()
        navigationState.tryEmit(navigation)
    }

    /**
     * Method use to notify a navigation back event
     */
    protected open fun navigateBack() {
        navigationState.tryEmit(null)
    }

    /**
     * When a background task must be executed for data retrieving or other background job, it must
     * be called through this method with [block] function
     * @param block is the function that will be executed in background
     * @param fullException If its is true, an exception launched on some child task affects to the
     * rest of task, including the parent one, if it is false, only affect to the child class
     * @return the job that can handle the lifecycle of the background task
     */
    protected fun executeUseCase(
        fullException: Boolean = false,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return concurrencyManager.launch(fullException = fullException, block = block)
    }

    /**
     * When a background task must be executed for data retrieving or other background job, it must
     * be called through this method with [block] function
     * @param block is the function that will be executed in background
     * @param exceptionBlock Function to handle errors
     * @param handleCancellationManually Function to handle Cancellation Exception in coroutine
     * @param fullException If its is true, an exception launched on some child task affects to the
     * rest of task, including the parent one, if it is false, only affect to the child class
     * @return the job that can handle the lifecycle of the background task
     */
    protected fun executeUseCaseWithException(
        block: suspend CoroutineScope.() -> Unit,
        exceptionBlock: suspend CoroutineScope.(Throwable) -> Unit,
        handleCancellationManually: Boolean = false,
        fullException: Boolean = false
    ): Job {
        return concurrencyManager.launch(fullException = fullException) {
            tryCatch(block, exceptionBlock, handleCancellationManually)
        }
    }

    /**
     * Method to override onCleared ViewModel method
     */
    protected open fun onDestroy() = Unit

    /**
     * Normal state content of the view
     */
    private lateinit var normalContentData: S

    private val emaResultHandler: EmaResultHandler = EmaResultHandler.getInstance()


    /**
     * Here should implement the listener for result data from other views through [addOnResultReceived] method
     */
    protected open fun onResultListenerSetup() = Unit


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

            is EmaState.Overlayed -> {
                val alternativeState = state as EmaState.Overlayed
                EmaState.Overlayed(newState, alternativeState.dataOverlayed)
            }
        }
    }

    /**
     * Update the current state and update the normal view state by default
     * @param changeStateFunction create the new state
     */
    protected open fun updateToNormalState(changeStateFunction: S.() -> S) {
        normalContentData = changeStateFunction.invoke(normalContentData)
        state = EmaState.Normal(normalContentData)
        updateToNormalState()
    }

    /**
     * Used for trigger an update on the view
     * Use the EmaState -> Normal
     */
    protected open fun updateToNormalState() {
        updateView(EmaState.Normal(normalContentData))
    }

    /**
     * Update the data of current state without notify it to the view.
     * @param changeStateFunction create the new state
     */
    protected open fun updateDataState(changeStateFunction: S.() -> S) {
        normalContentData = changeStateFunction.invoke(normalContentData)
        state = updateData(normalContentData)
    }


    /**
     * Get the current view state
     * @return the current viewState or null if it has not been initialized
     */
    fun getDataState(): S {
        return normalContentData
    }


    /**
     * Used for trigger an updateOverlayedState event on the view
     * Use the EmaState -> Alternative
     * @param data with updateOverlayedState information
     */
    protected open fun updateToOverlayedState(data: EmaExtraData? = null) {
        val overlayedData: EmaState.Overlayed<S> = data?.let {
            EmaState.Overlayed(normalContentData, dataOverlayed = it)
        } ?: EmaState.Overlayed(normalContentData)
        updateView(overlayedData)
    }

    /**
     * Used for trigger an updateErrorState event on the view
     * Use the EmaState -> Error
     * @param error with the exception object
     */
    protected open fun updateToErrorState(error: Throwable) {
        updateView(EmaState.Error(normalContentData, error))
    }

    /**
     * Set a result for previous view when the current one is destroyed
     */
    protected fun addResult(data: Any, code: Int = EmaResultHandler.RESULT_ID_DEFAULT) {
       useAfterStateIsCreated {
           emaResultHandler.addResult(
               EmaResultModel(
                   code = code,
                   ownerId = getId(),
                   data = data
               )
           )
       }
    }

    /**
     * Set the listener for back data when the result view is destroyed
     */
    protected fun addOnResultReceived(
        code: Int = EmaResultHandler.RESULT_ID_DEFAULT,
        receiver: (EmaResultModel) -> Unit
    ) {
        useAfterStateIsCreated {
            val emaReceiver = EmaReceiverModel(
                ownerId = getId(),
                resultCode = code,
                function = receiver
            )
            emaResultHandler.addResultReceiver(emaReceiver)
        }
    }

    /**
     * Method called when the ViewModel is destroyed. It cancels all background pending tasks.
     * Check call name for EmaAndroidView. It uses reflection to call this internal method
     */
    internal fun onCleared() {
        emaResultHandler.notifyResults(getId())
        concurrencyManager.cancelPendingTasks()
        useAfterStateIsCreated {
            onDestroy()
        }
    }

    fun getId(): Int {
        return this.javaClass.name.hashCode()
    }

    /**
     * Method called when physic back button is called
     * @return True if you wante the back pressed default behaviour is disabled. False you want the back pressed default behaviour is enabled
     */
    open fun onActionHardwareBackPressed(): Boolean {
        return false
    }
}