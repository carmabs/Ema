package com.carmabs.ema.core.viewmodel

import com.carmabs.ema.core.initializer.EmaInitializer
import com.carmabs.ema.core.state.EmaEffect
import com.carmabs.ema.core.state.EmaState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * View model to handle view states.
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
interface EmaViewModel<S : EmaState, E: EmaEffect> {

    val id: String
        get() {
            return "EmaViewModel ID: ${this.javaClass.name}"
        }
    val initialState: S

    /**
     * Used to know if subscribed view should render the state
     */
    val shouldRenderState:Boolean

    fun setScope(scope: CoroutineScope)

    fun onCreated(initializer: EmaInitializer? = null)

    fun onStartView()

    fun onResumeView()

    fun onPauseView()

    fun onStopView()

    fun onCleared()

    fun subscribeStateUpdates(): Flow<S>

    /**
     * Get effect state to be handled by the view
     */
    fun subscribeToEffectUpdates(): Flow<List<E>>

    fun consumeEffect(effect: E)


    object EMPTY : EmaViewModel<EmaState.EMPTY, EmaEffect.EMPTY> {


        override val initialState: EmaState.EMPTY = EmaState.EMPTY
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

        override fun subscribeStateUpdates(): Flow<EmaState.EMPTY> {
            return emptyFlow()
        }

        override fun subscribeToEffectUpdates(): Flow<List<EmaEffect.EMPTY>> {
            return emptyFlow()
        }

        override fun consumeEffect(effect: EmaEffect.EMPTY) {
            
        }
    }
}
