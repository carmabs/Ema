package com.carmabs.ema.core.viewmodel

import com.carmabs.ema.core.concurrency.ConcurrencyManager
import com.carmabs.ema.core.concurrency.DefaultConcurrencyManager
import com.carmabs.ema.core.concurrency.tryCatch
import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.ema.core.state.EmaBaseState
import com.carmabs.ema.core.state.EmaExtraData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*


/**
 *
 * Abstract base class for ViewModel in MVVM architecture.
 *
 * @param S is template about the class which will represent the state of the view. It has to implement
 * the [EmaBaseState] interface
 *
 * @param NS is the template about the available navigation states contained in the [EmaNavigator] used
 * for the feature that implement this ViewModel
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
abstract class EmaBaseViewModel<S : EmaBaseState, NS : EmaNavigationState> {

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
    internal val observableState: MutableSharedFlow<S> = MutableSharedFlow()

    /**
     * Observable state that launch event every time a value is set. This value will be a [EmaExtraData]
     * object that can contain any type of object. It will be used for
     * events that only has to be notified once to its observers, e.g: A toast message.
     */
    internal val singleObservableState: MutableSharedFlow<EmaExtraData> = MutableSharedFlow()

    /**
     * Observable state that launch event every time a value is set. [NS] value be will a [EmaNavigationState]
     * object that represent the destination. This observable will be used for
     * events that only has to be notified once to its observers and is used to notify the navigation
     * events
     */
    internal val navigationState: MutableSharedFlow<NS?> = MutableSharedFlow()

    /**
     * Manager to handle the threads where the background tasks are going to be launched
     */
    var concurrencyManager: ConcurrencyManager = DefaultConcurrencyManager()

    /**
     * The state of the view.
     */
    internal var state: S? = null

    /**
     * To determine if the view must be updated when view model is created automatically
     */
    protected open val updateOnInitialization: Boolean = true


    /**
     * Methos called the first time ViewModel is created
     * @param inputState
     * @return true if it's the first time is started
     */
    open fun onStart(inputState: S? = null): Boolean {
        val firstTime = if (state == null) {
            val initialStatus = inputState ?: createInitialState()
            state = initialStatus
            onStartFirstTime(inputState != null)
            true
        } else false

        state?.also {
            executeUseCase {
                if (updateOnInitialization)
                    observableState.emit(it)
                onResume(firstTime)
            }
        }

        return firstTime
    }

    /**
     * Called when view is created by first time, it means, it is added to the stack
     */
    abstract fun onStartFirstTime(statePreloaded: Boolean)

    /**
     * Called always the view goes to the foreground
     */
    abstract fun onResume(firstTime:Boolean)

    /**
     * Get observable state as LiveDaya to avoid state setting from the view
     */
    fun getObservableState(): SharedFlow<S> = observableState

    /**
     * Get current state of view
     */
    fun getCurrentState(): S? = state

    /**
     * Get navigation state as LiveData to avoid state setting from the view
     */
    fun getNavigationState(): SharedFlow<NS?> = navigationState

    /**
     * Get single state as LiveData to avoid state setting from the view
     */
    fun getSingleObservableState(): SharedFlow<EmaExtraData> = singleObservableState

    /**
     * Check the current view state
     * @param checkStateFunction function to check the current state
     */
    protected fun checkState(checkStateFunction: (S) -> Unit) {
        state?.run {
            checkStateFunction.invoke(this)
        }
    }

    /**
     * Method used to update the state of the view. It will be notified to the observers
     * @param state Tee current state of the view
     */
    protected open fun updateView(state: S) {
        this.state = state
        executeUseCase {
            observableState.emit(state)
        }
    }

    /**
     * Method used to notify to the observer for a single event that will be notified only once time.
     * It a new observer is attached, it will not be notified
     */
    protected open fun notifySingleEvent(extraData: EmaExtraData) {
        executeUseCase {
            singleObservableState.emit(extraData)
        }
    }

    /**
     * Method use to notify a navigation event
     * @param navigation The object that represent the destination of the navigation
     */
    protected open fun navigate(navigation: NS) {
        executeUseCase {
            navigationState.emit( navigation)
        }

    }

    /**
     * Method use to notify a navigation back event
     */
    protected open fun navigateBack() {
        executeUseCase {
            navigationState.emit( null)
        }
    }

    /**
     * Methods which must implement the default state of the view
     * @return the default state of the view
     */
    protected abstract fun createInitialState(): S

    /**
     * When a background task must be executed for data retrieving or other background job, it must
     * be called through this method with [block] function
     * @param block is the function that will be executed in background
     * @param fullException If its is true, an exception launched on some child task affects to the
     * rest of task, including the parent one, if it is false, only affect to the child class
     * @return the job that can handle the lifecycle of the background task
     */
    protected fun executeUseCase(fullException: Boolean = false, block: suspend CoroutineScope.() -> Unit): Job {
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
    protected fun executeUseCaseWithException(block: suspend CoroutineScope.() -> Unit,
                                              exceptionBlock: suspend CoroutineScope.(Throwable) -> Unit,
                                              handleCancellationManually: Boolean = false,
                                              fullException: Boolean = false): Job {
        return concurrencyManager.launch(fullException = fullException) {
            tryCatch(block, exceptionBlock, handleCancellationManually)
        }
    }

    /**
     * Method called when the ViewModel is destroyed. It cancell all background pending tasks
     */
    open fun onDestroy() {
        concurrencyManager.cancelPendingTasks()
    }
}