package com.carmabs.ema.core.viewmodel

import com.carmabs.ema.core.initializer.EmaInitializer
import com.carmabs.ema.core.state.EmaEvent
import com.carmabs.ema.core.state.EmaState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * View model to handle view states.
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
interface EmaViewModel<S : EmaState, E : EmaEvent> {

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

    val stateFlow: Flow<S>

    val eventFlow: Flow<List<E>>


    object EMPTY : EmaViewModel<EmaState.EMPTY, EmaEvent.EMPTY> {


        override val initialState: EmaState.EMPTY = EmaState.EMPTY
        override val stateFlow: Flow<EmaState.EMPTY> = emptyFlow()
        override val eventFlow: Flow<List<EmaEvent.EMPTY>> = emptyFlow()

        override val shouldRenderState: Boolean
            get() = true

        override fun setScope(scope: CoroutineScope) {

        }

        override fun onCreated(initializer: EmaInitializer?) {

        }

        override fun onStartView() {

        }

        override fun onResumeView() {

        }

        override fun onPauseView() {

        }

        override fun onStopView() {

        }

        override fun onCleared() {

        }
    }
}
