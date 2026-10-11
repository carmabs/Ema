package com.carmabs.emanate.core.source

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.source.sideeffect.SideEffect
import com.carmabs.emanate.core.source.sideeffect.SideEffectConcurrency
import com.carmabs.emanate.core.source.sideeffect.SideEffectScope
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

private data class EffectState(val text: String = "", val saved: Boolean = false) : EmaState

private sealed interface EffectAction : EmaAction {
    data object Load : EffectAction
    data class Loaded(val text: String) : EffectAction
    data object Save : EffectAction
    data object Saved : EffectAction
    data class Fetch(val id: Int, val duration: Long) : EffectAction
    data class Submit(val id: Int) : EffectAction
    data object Other : EffectAction
}

private sealed interface EffectEvent : EmaEvent {
    data object LoadedMessage : EffectEvent
    data object SavedMessage : EffectEvent
}

/**
 * Side effect written as a class: it receives the action already typed
 */
private class SaveSideEffect : SideEffect<EffectState, EffectAction, EffectEvent, EffectAction.Save> {

    override val actionType = EffectAction.Save::class

    override suspend fun SideEffectScope<EffectState, EffectAction, EffectEvent>.invoke(action: EffectAction.Save) {
        delay(1000)
        dispatch(EffectAction.Saved)
        postEvent(EffectEvent.SavedMessage)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class EmaSourceSideEffectTest {

    private val received = mutableListOf<String>()

    private fun TestScope.testSource(dispatcher: CoroutineDispatcher = UnconfinedTestDispatcher(testScheduler)) =
        EmaSource<EffectState, EffectAction, EffectEvent>(EffectState(), dispatcher) {
            addReducer<EffectAction.Load> { state.copy(text = "loading") }
            addReducer<EffectAction.Loaded> { action -> state.copy(text = action.text) }
            addReducer<EffectAction.Saved> { state.copy(saved = true) }
            addSideEffect<EffectAction.Load> {
                received += "effect sees ${state.text}"
                delay(1000)
                dispatch(EffectAction.Loaded("loaded"))
                postEvent(EffectEvent.LoadedMessage)
            }
            addSideEffect(SaveSideEffect())
            addSideEffect<EffectAction.Fetch>(
                concurrency = SideEffectConcurrency.Latest,
                timeout = 2.seconds,
                onTimeout = { action -> received += "timeout ${action.id}" }
            ) { action ->
                delay(action.duration)
                received += "fetched ${action.id}"
            }
            addSideEffect<EffectAction.Submit>(concurrency = SideEffectConcurrency.Leading) { action ->
                delay(1000)
                received += "submitted ${action.id}"
            }
        }

    @Test
    fun `side effects run after the reducers`() = runTest {
        val source = testSource()

        source.dispatch(EffectAction.Load)
        runCurrent()

        assertTrue("effect sees loading" in received)
        assertEquals("loading", source.state.text)
        source.close()
    }

    @Test
    fun `side effects dispatch actions and post events`() = runTest {
        val source = testSource()

        source.dispatch(EffectAction.Load)
        source.dispatch(EffectAction.Save)
        advanceTimeBy(1500)
        runCurrent()

        assertEquals(EffectState(text = "loaded", saved = true), source.state)
        assertEquals(listOf(EffectEvent.LoadedMessage, EffectEvent.SavedMessage), source.eventFlow.first())
        source.close()
    }

    @Test
    fun `events arrive after the actions dispatched before them`() = runTest {
        // The source runs on a dispatcher that queues its work, and the view collects the events immediately
        val source = testSource(StandardTestDispatcher(testScheduler))
        val textWhenLoaded = mutableListOf<String>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            source.eventFlow.collect { textWhenLoaded += source.state.text }
        }

        source.dispatch(EffectAction.Load)
        advanceTimeBy(1500)
        runCurrent()

        assertEquals(listOf("loaded"), textWhenLoaded)
        source.close()
    }

    @Test
    fun `events can be consumed`() = runTest {
        val source = testSource()
        source.dispatch(EffectAction.Load)
        advanceTimeBy(1500)
        runCurrent()

        source.consumeEvent(EffectEvent.LoadedMessage)
        source.dispatch(EffectAction.Load)
        advanceTimeBy(1500)
        runCurrent()

        assertEquals(listOf(EffectEvent.LoadedMessage), source.eventFlow.first())
        source.close()
    }

    @Test
    fun `latest side effects cancel the running one`() = runTest {
        val source = testSource()

        source.dispatch(EffectAction.Fetch(id = 1, duration = 1000))
        advanceTimeBy(100)
        // Other actions don't cancel it
        source.dispatch(EffectAction.Other)
        advanceTimeBy(100)
        source.dispatch(EffectAction.Fetch(id = 2, duration = 1000))
        advanceTimeBy(1500)
        runCurrent()

        assertEquals(listOf("fetched 2"), received.filter { it.startsWith("fetched") })
        source.close()
    }

    @Test
    fun `other actions don't cancel a running side effect`() = runTest {
        val source = testSource()

        source.dispatch(EffectAction.Fetch(id = 1, duration = 1000))
        advanceTimeBy(100)
        source.dispatch(EffectAction.Other)
        advanceTimeBy(1500)
        runCurrent()

        assertEquals(listOf("fetched 1"), received.filter { it.startsWith("fetched") })
        source.close()
    }

    @Test
    fun `leading side effects ignore the actions while running`() = runTest {
        val source = testSource()

        source.dispatch(EffectAction.Submit(1))
        advanceTimeBy(100)
        source.dispatch(EffectAction.Submit(2))
        advanceTimeBy(1500)
        source.dispatch(EffectAction.Submit(3))
        advanceTimeBy(1500)
        runCurrent()

        assertEquals(listOf("submitted 1", "submitted 3"), received.filter { it.startsWith("submitted") })
        source.close()
    }

    @Test
    fun `side effects that don't finish in time call onTimeout`() = runTest {
        val source = testSource()

        source.dispatch(EffectAction.Fetch(id = 1, duration = 5000))
        advanceTimeBy(6000)
        runCurrent()

        assertEquals(listOf("timeout 1"), received.filter { it.startsWith("fetched") || it.startsWith("timeout") })
        source.close()
    }

    @Test
    fun `observers receive the actions states and events in order`() = runTest {
        val source = testSource()
        val observed = mutableListOf<String>()
        source.addObserver(object : EmaSourceObserver<EffectState, EffectEvent> {
            override fun onAction(action: EmaAction) {
                observed += "action $action"
            }

            override fun onState(state: EffectState) {
                observed += "state $state"
            }

            override fun onEvent(event: EffectEvent) {
                observed += "event $event"
            }
        })

        source.dispatch(EffectAction.Save)
        advanceTimeBy(1500)
        runCurrent()

        assertEquals(
            listOf(
                "action Save",
                "state EffectState(text=, saved=false)",
                "action Saved",
                "state EffectState(text=, saved=true)",
                "event SavedMessage"
            ),
            observed
        )
        source.close()
    }

    @Test
    fun `closing the source cancels its side effects`() = runTest {
        val source = testSource()

        source.dispatch(EffectAction.Save)
        val closed = source.close()
        advanceTimeBy(1500)
        runCurrent()

        assertTrue(closed.isCompleted)
        assertEquals(EffectState(), source.state)
    }
}
