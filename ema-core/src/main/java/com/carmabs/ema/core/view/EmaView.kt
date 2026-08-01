package com.carmabs.ema.core.view

import com.carmabs.ema.core.action.EmaEventDispatcher
import com.carmabs.ema.core.initializer.EmaInitializerSerializer
import com.carmabs.ema.core.navigator.EmaNavigator
import com.carmabs.ema.core.state.EmaEvent
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
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
interface EmaView<S : EmaState, VM : EmaViewModel<S, E>, E : EmaEvent> {

    /**
     * Scope for flow updates
     */
    val coroutineScope: CoroutineScope

    /**
     * The view model [EmaViewModel] for the view
     */
    val viewModel: VM

    /**
     * The navigator [EmaNavigator]
     */
    val navigator: EmaNavigator<E>?

    /**
     * The initializer from previous views when it is launched.
     */
    val initializerSerializer: EmaInitializerSerializer?

    /**
     * The previous state of the View
     */
    var previousState: S?


    /**
     * Trigger to start viewmodel only when startViewModel is launched
     */
    val startTrigger: EmaViewModelTrigger?

    /**
     * Called when view model trigger an update view event
     * @param state of the view
     */
    private suspend fun onStateUpdated(state: S) {
        onState(state)
        previousState = state
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
        areEqualComparator: ((old: T, new: T) -> Boolean)? = null,
        action: (new: T) -> Unit
    ): Boolean {
        var updated = false
        val currentClass = (field as PropertyReference0).boundReceiver as? S
        currentClass?.also { _ ->
            val currentValue = (field.get() as T)
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
        areEqualComparator: ((old: T, new: T) -> Boolean)? = null,
        action: (old: T?, new: T) -> Unit
    ): Boolean {
        var updated = false
        val currentClass = (field as PropertyReference0).boundReceiver as? S
        currentClass?.also { _ ->
            val currentValue = (field.get() as T)
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
     * Called when view model trigger an update view event
     * @param state with the state of the view
     */
    fun onState(state: S)

    /**
     * Called when view model trigger an effect
     * @param effect effect dispatched by viewmodel
     */
    suspend fun onEffect(effect: E)

    fun navigate(navigationEvent: E) {
        navigator?.navigate(navigationEvent) ?: throwNavigationException()
    }

    @Throws
    private fun throwNavigationException() {
        throw RuntimeException("You must provide an EmaNavigator as navigator to handle the navigation")
    }

    /**
     * Called when view model trigger a navigation back event
     * @return True
     */
    fun navigateBack(result: Any? = null): Boolean {
        return navigator?.navigateBack(result) ?: onBack(result)
    }

    fun onBack(result: Any?): Boolean

    fun onCreate(viewModel: VM) {
        startTrigger?.also {
            it.triggerAction = {
                viewModel.onCreated(initializerSerializer?.restore())
            }
        } ?: also {
            viewModel.onCreated(initializerSerializer?.restore())
        }
    }

    /**
     * Called when view model is started
     */
    fun onStartView(viewModel: VM) {
        startTrigger?.also {
            it.triggerAction = {
                viewModel.onStartView()
            }
        } ?: also {
            viewModel.onStartView()
        }
    }


    /**
     * Called when view state is bound to viewmodel
     */
    fun onBindView(coroutineScope: CoroutineScope, viewModel: VM): MutableList<Job> {
        val jobList = mutableListOf<Job>()
        jobList.add(onBindState(coroutineScope, viewModel))
        jobList.add(onBindEffects(coroutineScope, viewModel))
        return jobList
    }

    /**
     * Called when view state is bound to viewmodel
     */

    fun onBindState(coroutineScope: CoroutineScope, viewModel: VM): Job {
        return coroutineScope.launch {
            viewModel.stateFlow.collectLatest {
                onStateUpdated(it)
            }
        }
    }

    fun onBindEffects(coroutineScope: CoroutineScope, viewModel: VM): Job {
        return coroutineScope.launch {
            viewModel.eventFlow.collectLatest {
                it.forEach { effect ->
                    onEffect(effect)
                    (viewModel as? EmaEventDispatcher<E>)?.consumeEvent(effect)
                }
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


    fun onUnbindView(viewJob: MutableList<Job>?, viewModel: VM) {
        viewJob?.forEach {
            try {
                if (!it.isCancelled && !it.isCompleted)
                    it.cancel()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        viewJob?.clear()
        viewModel.onStopView()
    }
}
