package com.carmabs.ema.compose

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.carmabs.ema.compose.action.EmaImmutableActionDispatcher
import com.carmabs.ema.compose.ui.EmaComposableScreenContent
import com.carmabs.ema.core.action.EmaAction
import com.carmabs.ema.core.state.EmaEvent
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.viewmodel.EmaViewModelAction
import com.carmabs.ema.core.viewmodel.EmaViewModelBasic
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

data class CounterState(val count: Int = 0) : EmaState

sealed interface CounterAction : EmaAction {
    data object Increment : CounterAction
    data object Back : CounterAction
    data object Notify : CounterAction
}

sealed interface CounterEvent : EmaEvent {
    data object Notified : CounterEvent
}

data class RouteInitializer(val name: String) : EmaAction.Initializer

/**
 * Serializer written by hand, so the tests do not need the serialization plugin.
 */
object RouteInitializerSerializer : KSerializer<RouteInitializer> {
    override val descriptor = PrimitiveSerialDescriptor("RouteInitializer", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: RouteInitializer) = encoder.encodeString(value.name)
    override fun deserialize(decoder: Decoder) = RouteInitializer(decoder.decodeString())
}

class CounterViewModel : EmaViewModelAction<CounterState, CounterAction, CounterEvent>(CounterState()) {
    val hooks = mutableListOf<String>()
    var initializer: EmaAction.Initializer? = null

    override fun onStateCreated(initializer: EmaAction.Initializer?) {
        hooks += "created"
        this.initializer = initializer
    }

    override fun onViewStarted() {
        hooks += "started"
    }

    override fun onViewResumed() {
        hooks += "resumed"
    }

    override fun onViewPaused() {
        hooks += "paused"
    }

    override fun onViewStopped() {
        hooks += "stopped"
    }

    override fun onAction(action: CounterAction) {
        when (action) {
            CounterAction.Increment -> updateState { copy(count = count + 1) }
            CounterAction.Back -> hooks += "back"
            CounterAction.Notify -> postEvent(CounterEvent.Notified)
        }
    }
}

/**
 * ViewModel that does not handle actions.
 */
class BasicViewModel : EmaViewModelBasic<CounterState, CounterEvent>(CounterState()) {
    override fun onStateCreated(initializer: EmaAction.Initializer?) = Unit
}

class CounterScreen(private val backEnabled: Boolean = true) :
    EmaComposableScreenContent<CounterState, CounterAction, CounterEvent> {
    val events = mutableListOf<CounterEvent>()

    override suspend fun onEvent(
        context: Context,
        event: CounterEvent,
        actions: EmaImmutableActionDispatcher<CounterAction>
    ) {
        events += event
    }

    override fun onBack(state: CounterState) = if (backEnabled) CounterAction.Back else null

    @Composable
    override fun onState(state: CounterState, actions: EmaImmutableActionDispatcher<CounterAction>) {
        Column {
            BasicText("Count ${state.count}")
            BasicText("Increment", Modifier.clickable { actions.dispatch(CounterAction.Increment) })
            BasicText("Notify", Modifier.clickable { actions.dispatch(CounterAction.Notify) })
        }
    }
}

/**
 * Screen that keeps the default event and back handling.
 */
class SilentScreen : EmaComposableScreenContent<CounterState, CounterAction, CounterEvent> {
    @Composable
    override fun onState(state: CounterState, actions: EmaImmutableActionDispatcher<CounterAction>) {
        BasicText("Silent ${state.count}", Modifier.clickable { actions.dispatch(CounterAction.Notify) })
    }
}
