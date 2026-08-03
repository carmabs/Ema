package com.carmabs.ema.core.viewmodel

import com.carmabs.ema.core.action.EmaEventDispatcher
import com.carmabs.ema.core.initializer.EmaInitializer
import com.carmabs.ema.core.state.EmaEvent
import com.carmabs.ema.core.state.EmaState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow

/**
 * View model to handle view states.
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
interface EmaViewModel<S : EmaState, E : EmaEvent>: EmaEventDispatcher<E> {

    val id: String
        get() {
            return "EmaViewModel ID: ${this.javaClass.name}"
        }
    val initialState: S

    /**
     * Used to know if subscribed view should render the state
     */
    val shouldRenderState: Boolean

    fun setScope(scope: CoroutineScope)

    fun onCreated(initializer: EmaInitializer? = null)

    fun onStartView()

    fun onResumeView()

    fun onPauseView()

    fun onStopView()

    fun onCleared()

    val stateFlow: StateFlow<S>

    object EMPTY : EmaViewModel<EmaState.EMPTY, EmaEvent.EMPTY> {
        override val initialState: EmaState.EMPTY = EmaState.EMPTY

        override val stateFlow: StateFlow<EmaState.EMPTY> = MutableStateFlow(initialState)

        override fun consumeEvent(event: EmaEvent.EMPTY) = Unit

        override val eventFlow: Flow<List<EmaEvent.EMPTY>> = emptyFlow()

        override val shouldRenderState: Boolean
            get() = true

        override fun setScope(scope: CoroutineScope) = Unit

        override fun onCreated(initializer: EmaInitializer?) = Unit

        override fun onStartView() = Unit

        override fun onResumeView() = Unit

        override fun onPauseView() = Unit

        override fun onStopView() = Unit

        override fun onCleared() = Unit
    }
}
