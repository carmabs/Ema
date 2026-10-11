package com.carmabs.emanate.core.viewmodel

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.action.EmaEventDispatcher
import com.carmabs.emanate.core.extension.emaName
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Contract between a view and its view model: the state and the events the view observes and the lifecycle it
 * notifies. It is implemented by [EmaViewModelBasic] and [EmaViewModelAction].
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
interface EmaViewModelContract<S : EmaState, E : EmaEvent> : EmaEventDispatcher<E> {

    val id: String
        get() {
            return "EmaViewModel ID: ${this::class.emaName}"
        }
    val initialState: S

    /**
     * Used to know if subscribed view should render the state
     */
    val shouldRenderState: Boolean

    fun onCreated(initializer: EmaAction.Initializer? = null)

    fun onStartView()

    fun onResumeView()

    fun onPauseView()

    fun onStopView()

    fun onCleared()

    val stateFlow: StateFlow<S>

    object EMPTY : EmaViewModelContract<EmaState.EMPTY, EmaEvent.EMPTY> {
        override val initialState: EmaState.EMPTY = EmaState.EMPTY

        override val stateFlow: StateFlow<EmaState.EMPTY> = MutableStateFlow(initialState)

        override fun consumeEvent(event: EmaEvent.EMPTY) = Unit

        override val eventFlow: Flow<List<EmaEvent.EMPTY>> = emptyFlow()

        override val shouldRenderState: Boolean
            get() = true

        override fun onCreated(initializer: EmaAction.Initializer?) = Unit

        override fun onStartView() = Unit

        override fun onResumeView() = Unit

        override fun onPauseView() = Unit

        override fun onStopView() = Unit

        override fun onCleared() = Unit
    }
}
