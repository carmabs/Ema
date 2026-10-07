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
     * Called when view model trigger an update view event
     * @param state with the state of the view
     */
    fun onState(state: S)

    /**
     * Called when view model trigger an event
     * @param event event dispatched by viewmodel
     */
    suspend fun onEvent(event: E)

    fun navigate(navigationEvent: E) {
        navigator?.navigate(navigationEvent) ?: throwNavigationException()
    }

    private fun throwNavigationException(): Unit =
        throw RuntimeException("You must provide an EmaNavigator as navigator to handle the navigation")

    /**
     * Called when view model trigger a navigation back event
     * @return True
     */
    fun navigateBack(result: Any? = null): Boolean = navigator?.navigateBack(result) ?: onBack(result)

    fun onBack(result: Any?): Boolean

    fun onCreate(viewModel: VM) {
        startTrigger?.runWhenStarted {
            viewModel.onCreated(initializerSerializer?.restore())
        } ?: viewModel.onCreated(initializerSerializer?.restore())
    }

    /**
     * Called when view model is started
     */
    fun onStartView(viewModel: VM) {
        startTrigger?.runWhenStarted {
            viewModel.onStartView()
        } ?: viewModel.onStartView()
    }

    /**
     * Called when view state is bound to viewmodel
     */
    fun onBindView(coroutineScope: CoroutineScope, viewModel: VM): MutableList<Job> {
        val jobList = mutableListOf<Job>()
        jobList.add(onBindState(coroutineScope, viewModel))
        jobList.add(onBindEvents(coroutineScope, viewModel))
        return jobList
    }

    /**
     * Called when view state is bound to viewmodel
     */

    fun onBindState(coroutineScope: CoroutineScope, viewModel: VM): Job = coroutineScope.launch {
        viewModel.stateFlow.collectLatest {
            onStateUpdated(it)
        }
    }

    fun onBindEvents(coroutineScope: CoroutineScope, viewModel: VM): Job = coroutineScope.launch {
        viewModel.eventFlow.collectLatest {
            it.forEach { event ->
                onEvent(event)
                (viewModel as? EmaEventDispatcher<E>)?.consumeEvent(event)
            }
        }
    }

    /**
     * Used to notify the view model that view has been gone to foreground.
     */
    fun onResumeView(viewModel: VM) {
        startTrigger?.also {
            if (it.hasBeenStarted) {
                viewModel.onResumeView()
            }
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
                if (!it.isCancelled && !it.isCompleted) {
                    it.cancel()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        viewJob?.clear()
        viewModel.onStopView()
    }
}
