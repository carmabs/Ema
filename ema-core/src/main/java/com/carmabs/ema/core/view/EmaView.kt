package com.carmabs.ema.core.view

import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.ema.core.navigator.EmaNavigator
import com.carmabs.ema.core.state.EmaBaseState
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.viewmodel.EmaViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.jvm.internal.PropertyReference0
import kotlin.reflect.KProperty


/**
 * View to handle VM view logic states through [EmaState].
 * The user must provide in the constructor by template:
 *  - The view model class [EmaViewModel] is going to use the view
 *  - The navigation state class [EmaNavigationState] will handle the navigation
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
interface EmaView<S : EmaBaseState, VM : EmaViewModel<S, NS>, NS : EmaNavigationState> {

    companion object {
        const val KEY_INPUT_STATE_DEFAULT = "EMA_KEY_INPUT_STATE_DEFAULT"
    }

    /**
     * Scope for flow updates
     */
    val coroutineScope: CoroutineScope

    /**
     * The view mdeol seed [EmaViewModel] for the view
     */
    val viewModelSeed: VM

    /**
     * The navigator [EmaNavigator]
     */
    val navigator: EmaNavigator<NS>?

    /**
     * The state set up form previous views when it is launched.
     */
    val inputState: S?

    /**
     * The previous state of the View
     */
    var previousState: S?


    /**
     * Determine if the previousState updates automatically after onNormalState or if
     * has to be set up  manually
     */
    val updatePreviousStateAutomatically: Boolean


    /**
     * Trigger to start viewmodel only when startViewModel is launched
     */
    val startTrigger: EmaViewModelTrigger?

    /**
     * Called when view model trigger an update view event
     * @param state of the view
     */
    private fun onDataUpdated(state: EmaState<S>) {
        onEmaStateNormal(state.data)
        when (state) {
            is EmaState.Overlayed -> {
                onStateOverlayed(state.dataOverlayed)
            }
            is EmaState.Error -> {
                onEmaStateError(state.error)
            }
        }

        if (updatePreviousStateAutomatically)
            previousState = state.data
    }

    /**
     * Check EMA state selected property to execute action with new value if it has changed
     * @param action Action to execute. Current value passed in lambda.
     * @param field Ema State field to check if it has been changed.
     * @param areEqualComparator Comparator to determine if both objects are equals. Useful for complex objects
     * @return true if it has been updated, false otherwise
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> bindForUpdate(
        field: KProperty<T>,
        areEqualComparator: ((old: T?, new: T?) -> Boolean)? = null,
        action: (new: T?) -> Unit
    ): Boolean {
        var updated = false
        val currentClass = (field as PropertyReference0).boundReceiver as? S
        currentClass?.also { _ ->
            val currentValue = field.get() as T
            previousState?.also {
                try {
                    val previousField = it.javaClass.getDeclaredField(field.name)
                    previousField.isAccessible = true
                    val previousValue = previousField.get(previousState) as T
                    if (areEqualComparator?.invoke(previousValue, currentValue)?.not()
                            ?: (previousValue != currentValue)
                    ) {
                        updated = true
                        action.invoke(currentValue)
                    }
                } catch (e: Exception) {
                    println("EMA : Field not found")
                }
            } ?: action.invoke(currentValue)
        } ?: println("EMA : Bounding class must be the state of the view")
        return updated
    }

    /**
     * Check EMA state selected property to execute action with new value if it has changed
     * @param action Action to execute. Current and previous value passed in lambda
     * @param field Ema State field to check if it has been changed
     * @param areEqualComparator Comparator to determine if both objects are equals. Useful for complex objects
     * @return true if it has been updated, false otherwise
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> bindForUpdateWithPrevious(
        field: KProperty<T>,
        areEqualComparator: ((old: T?, new: T?) -> Boolean)? = null,
        action: (old: T?, new: T?) -> Unit
    ): Boolean {
        var updated = false
        val currentClass = (field as PropertyReference0).boundReceiver as? S
        currentClass?.also { _ ->
            val currentValue = field.get() as T
            previousState?.also {
                try {
                    val previousField = it.javaClass.getDeclaredField(field.name)
                    previousField.isAccessible = true
                    val previousValue = previousField.get(previousState) as T
                    if (areEqualComparator?.invoke(previousValue, currentValue)?.not()
                            ?: (previousValue != currentValue)
                    ) {
                        updated = true
                        action.invoke(previousValue, currentValue)
                    }
                } catch (e: Exception) {
                    println("EMA : Field not found")
                }
            } ?: action.invoke(null, currentValue)
        } ?: println("EMA : Bounding class must be the state of the view")
        return updated
    }

    /**
     * Called when view model trigger an only once notified event
     * @param data for extra information
     */
    fun onSingleData(data: EmaExtraData) {
        onSingleEvent(data)
    }

    /**
     * Called when view model trigger an only once notified event for navigation
     * @param navigation state with information about the destination
     */
    fun onNavigation(navigation: EmaNavigationState?) {
        navigation?.let {
            navigate(navigation)
        } ?: navigateBack()
    }

    /**
     * Called when view model trigger an update view event
     * @param data with the state of the view
     */
    fun onEmaStateNormal(data: S)

    /**
     * Called when view model trigger a updateOverlayedState event
     * @param data with information about updateOverlayedState
     */
    fun onStateOverlayed(data: EmaExtraData)

    /**
     * Called when view model trigger an only once notified event.Not called when the view is first time attached to the view model
     * @param data with information about updateAlternativeState
     */
    fun onSingleEvent(data: EmaExtraData)

    /**
     * Called when view model trigger an error event
     * @param error generated by view model
     */
    fun onEmaStateError(error: Throwable)

    /**
     * Called when view model trigger a navigation event
     * @param state with info about destination
     */
    @Suppress("UNCHECKED_CAST")
    fun navigate(state: EmaNavigationState) {
        navigator?.navigate(state as NS)
    }

    /**
     * Called when view model trigger a navigation back event
     * @return True
     */
    fun navigateBack(): Boolean {
        return navigator?.navigateBack() ?: false
    }

    /**
     * Called when view model is started
     */
    fun onStartView(viewModel: VM) {
        startTrigger?.also {
            it.triggerAction = {
                viewModel.onStart(inputState?.let { input -> EmaState.Normal(input) })
            }
        } ?: also {
            viewModel.onStart(inputState?.let { input -> EmaState.Normal(input) })
        }
    }


    /**
     * Called when view state is bound to viewmodel
     */
    fun onBindView(coroutineScope: CoroutineScope, viewModel: VM): MutableList<Job> {
        val jobList = mutableListOf<Job>()
        jobList.add(onBindState(coroutineScope, viewModel))
        jobList.add(onBindSingle(coroutineScope, viewModel))
        jobList.add(onBindNavigation(coroutineScope, viewModel))
        return jobList
    }

    /**
     * Called when view state is bound to viewmodel
     */

    fun onBindState(coroutineScope: CoroutineScope, viewModel: VM): Job {
        return coroutineScope.launch {
            viewModel.getObservableState().collectLatest {
                onDataUpdated(it)
            }
        }
    }

    fun onBindNavigation(coroutineScope: CoroutineScope, viewModel: VM): Job {
        return coroutineScope.launch {
            viewModel.getNavigationState().collectLatest {
                onNavigation(it)
            }
        }
    }

    fun onBindSingle(coroutineScope: CoroutineScope, viewModel: VM): Job {
        return coroutineScope.launch {
            viewModel.getSingleObservableState().collect {
                onSingleData(it)
            }
        }
    }

    /**
     * Used to notify the view model that view has been gone to foreground.
     */
    fun onResumeView(viewModel: VM) {
        startTrigger?.also {
            if (it.hasBeenStarted)
                viewModel.onResumeView()
        } ?: also {
            viewModel.onResumeView()
        }
    }


    fun onPauseView(viewModel: VM) {
        viewModel.onPauseView()
    }

    fun onUnbindView(viewJob: MutableList<Job>?) {
        viewJob?.forEach {
            try {
                if (!it.isCancelled && !it.isCompleted)
                    it.cancel()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        viewJob?.clear()
    }
}