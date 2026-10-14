package com.carmabs.emanate.core.viewmodel

import com.carmabs.emanate.core.Emanate
import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.initializer.EmaInitializer
import com.carmabs.emanate.core.logging.EmaLogger
import com.carmabs.emanate.core.model.EmaConfiguration
import com.carmabs.emanate.core.source.middleware.middlewareDelayOf
import com.carmabs.emanate.core.source.reducer.reducerOf
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

private data class TestState(val count: Int = 0, val text: String = "", val resumed: Boolean = false) : EmaState

private sealed interface TestAction : EmaAction {
    data object Increment : TestAction
    data object Decrement : TestAction
    data class Update(val text: String) : TestAction
    data object Load : TestAction
    data object Slow : TestAction
}

private sealed interface TestEvent : EmaEvent {
    data object Loaded : TestEvent
}

private data class TestInitializer(val text: String) : EmaInitializer

/**
 * Result sent by the view model of other screen
 */
private data class TextSent(val text: String) : EmaAction

/**
 * Use case of the view model, to check that it can be used inside the scopes
 */
private class LoadUseCase {
    suspend operator fun invoke(): String {
        delay(1000)
        return "loaded"
    }
}

/**
 * View model of other screen that sends a result
 */
private class SenderViewModel(dispatcher: CoroutineDispatcher) :
    EmaViewModel<TestState, TestAction, TestEvent>(TestState(), dispatcher = dispatcher, logger = null) {

    override fun setup() = source {
        sideEffects {
            add<TestAction.Update> { action -> broadcast(TextSent(action.text)) }
        }
    }
}

/**
 * View model with a side effect for every lifecycle action
 */
private class LifecycleViewModel(dispatcher: CoroutineDispatcher) :
    EmaViewModel<TestState, TestAction, TestEvent>(TestState(), dispatcher = dispatcher, logger = null) {

    val launched = mutableListOf<String>()

    override fun setup() = source {
        sideEffects {
            add<EmaAction.Lifecycle> { action -> launched += action::class.simpleName.orEmpty() }
        }
    }
}

/**
 * View model that sends a result when it is destroyed, after [duration]
 */
private class ClosingViewModel(
    dispatcher: CoroutineDispatcher,
    private val duration: Duration,
    configuration: EmaConfiguration = Emanate.configuration
) : EmaViewModel<TestState, TestAction, TestEvent>(TestState(), configuration, dispatcher, logger = null) {

    val finished = mutableListOf<String>()

    override fun setup() = source {
        sideEffects {
            add<EmaAction.Lifecycle.Destroyed> {
                delay(duration)
                broadcast(TextSent("closed"))
                finished += "destroyed"
            }
        }
    }
}

private class TestViewModel(
    dispatcher: CoroutineDispatcher,
    private val loadUseCase: LoadUseCase = LoadUseCase(),
    logger: EmaLogger? = null,
    private val renderOnInitialization: Boolean = true
) : EmaViewModel<TestState, TestAction, TestEvent>(TestState(), dispatcher = dispatcher, logger = logger) {

    val received = mutableListOf<String>()

    override val updateOnInitialization: Boolean
        get() = renderOnInitialization

    /**
     * Reducer declared as a property of the view model
     */
    private val decrementReducer = reducerOf(TestAction.Decrement::class) { state.copy(count = state.count - 1) }

    override fun setup() = source {
        middlewares {
            add<EmaAction.Lifecycle> { action ->
                received += "lifecycle ${action::class.simpleName}"
                next()
            }
            add<EmaInitializer> { action ->
                received += "initializer $action"
                next()
            }
            +middlewareDelayOf(TestAction.Slow::class, 1.seconds)
        }
        reducers {
            add<TestAction.Increment> { state.copy(count = state.count + 1) }
            +decrementReducer
            add<TestAction.Slow> { state.copy(count = state.count + 1) }
            add<TestAction.Update> { action -> state.copy(text = action.text) }
            add<TestInitializer> { action -> state.copy(text = action.text) }
            add<EmaAction.Lifecycle.Resumed> { state.copy(resumed = true) }
            add<TextSent> { action -> state.copy(text = action.text) }
            add<TestAction.Load> { state.copy(text = "loading") }
        }
        sideEffects {
            add<TestAction.Load> {
                dispatch(TestAction.Update(loadUseCase()))
                postEvent(TestEvent.Loaded)
            }
        }
        broadcasts {
            listen<TextSent>()
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class EmaViewModelTest {

    private fun TestScope.testDispatcher() = UnconfinedTestDispatcher(testScheduler)

    @Test
    fun `reducers publish the new state`() = runTest {
        val viewModel = TestViewModel(testDispatcher())

        viewModel.dispatch(TestAction.Increment)
        viewModel.dispatch(TestAction.Increment)
        viewModel.dispatch(TestAction.Decrement)
        viewModel.dispatch(TestAction.Update("text"))
        runCurrent()

        assertEquals(TestState(count = 1, text = "text"), viewModel.stateFlow.value)
        viewModel.onCleared()
    }

    @Test
    fun `the initial state is the one of the constructor`() = runTest {
        val viewModel = TestViewModel(testDispatcher())

        assertEquals(TestState(), viewModel.initialState)
        assertEquals(TestState(), viewModel.stateFlow.value)
    }

    @Test
    fun `middlewares can delay the actions of the view model`() = runTest {
        val viewModel = TestViewModel(testDispatcher())

        viewModel.dispatch(TestAction.Slow)
        runCurrent()
        assertEquals(0, viewModel.stateFlow.value.count)

        advanceTimeBy(1500)
        runCurrent()
        assertEquals(1, viewModel.stateFlow.value.count)
        viewModel.onCleared()
    }

    @Test
    fun `use cases can be used in side effects`() = runTest {
        val viewModel = TestViewModel(testDispatcher())

        viewModel.dispatch(TestAction.Load)
        advanceTimeBy(1500)
        runCurrent()

        assertEquals("loaded", viewModel.stateFlow.value.text)
        assertEquals(listOf(TestEvent.Loaded), viewModel.eventFlow.first())
        viewModel.onCleared()
    }

    @Test
    fun `events can be consumed`() = runTest {
        val viewModel = TestViewModel(testDispatcher())
        viewModel.dispatch(TestAction.Load)
        advanceTimeBy(1500)
        runCurrent()

        viewModel.consumeEvent(TestEvent.Loaded)
        viewModel.dispatch(TestAction.Load)
        advanceTimeBy(1500)
        runCurrent()

        assertEquals(listOf(TestEvent.Loaded), viewModel.eventFlow.first())
        viewModel.onCleared()
    }

    @Test
    fun `middlewares and reducers receive the initializer and the lifecycle`() = runTest {
        val viewModel = TestViewModel(testDispatcher())

        viewModel.onCreated(TestInitializer("initial"))
        viewModel.onStartView()
        viewModel.onResumeView()
        viewModel.onPauseView()
        viewModel.onStopView()
        runCurrent()

        assertEquals(
            listOf(
                "initializer TestInitializer(text=initial)",
                "lifecycle Started",
                "lifecycle Resumed",
                "lifecycle Paused",
                "lifecycle Stopped"
            ),
            viewModel.received
        )
        assertEquals(TestState(text = "initial", resumed = true), viewModel.stateFlow.value)
        viewModel.onCleared()
    }

    @Test
    fun `the initializer is received only the first time`() = runTest {
        val viewModel = TestViewModel(testDispatcher())

        viewModel.onCreated(TestInitializer("first"))
        viewModel.onCreated(TestInitializer("second"))
        runCurrent()

        assertEquals("first", viewModel.stateFlow.value.text)
        viewModel.onCleared()
    }

    @Test
    fun `without initializer the empty one is received`() = runTest {
        val viewModel = TestViewModel(testDispatcher())

        viewModel.onCreated()
        runCurrent()

        assertEquals(listOf("initializer ${EmaInitializer.EMPTY}"), viewModel.received)
        viewModel.onCleared()
    }

    @Test
    fun `destroyed launches side effects`() = runTest {
        val viewModel = LifecycleViewModel(testDispatcher())

        viewModel.onResumeView()
        viewModel.onCleared()
        runCurrent()

        assertEquals(listOf("Resumed", "Destroyed"), viewModel.launched)
    }

    @Test
    fun `the side effects of destroyed can finish after the view model is cleared`() = runTest {
        val receiver = TestViewModel(testDispatcher())
        receiver.onCreated(null)
        val closing = ClosingViewModel(testDispatcher(), 1.seconds)
        closing.onCreated(null)

        closing.onCleared()
        advanceTimeBy(1500)
        runCurrent()

        assertEquals(listOf("destroyed"), closing.finished)
        assertEquals("closed", receiver.stateFlow.value.text)
        receiver.onCleared()
    }

    @Test
    fun `the time of the side effects of destroyed is set in the configuration`() = runTest {
        val closing = ClosingViewModel(testDispatcher(), 3.seconds, EmaConfiguration(destroyedTimeout = 2.seconds))
        closing.onCreated(null)

        closing.onCleared()
        advanceUntilIdle()

        assertTrue(closing.finished.isEmpty())
    }

    @Test
    fun `the logs print the action and the states with the printer of the configuration of the view model`() = runTest {
        val logs = mutableListOf<String>()
        val configuration = EmaConfiguration(
            dispatchers = EmaDispatchers(main = testDispatcher()),
            logger = EmaLogger { _, message -> logs += message },
            printer = { "printed ${it!!::class.simpleName}" }
        )
        val viewModel = object : EmaViewModel<TestState, TestAction, TestEvent>(TestState(), configuration) {
            override fun setup() = source {
                reducers {
                    add<TestAction.Increment> { state.copy(count = state.count + 1) }
                }
            }
        }

        viewModel.dispatch(TestAction.Increment)

        assertTrue(logs.any { "printed Increment" in it })
        assertEquals(2, logs.count { "printed TestState" in it })
        viewModel.onCleared()
    }

    @Test
    fun `the dispatcher and the logger are taken from the configuration of the view model`() = runTest {
        val logs = mutableListOf<String>()
        val configuration = EmaConfiguration(
            dispatchers = EmaDispatchers(main = testDispatcher()),
            logger = EmaLogger { tag, _ -> logs += tag }
        )
        val viewModel = object : EmaViewModel<TestState, TestAction, TestEvent>(TestState(), configuration) {
            override fun setup() = source {
                reducers {
                    add<TestAction.Increment> { state.copy(count = state.count + 1) }
                }
            }
        }

        viewModel.dispatch(TestAction.Increment)

        // Run by the test dispatcher of the configuration and logged by its logger, not by the ones of Emanate
        assertEquals(1, viewModel.stateFlow.value.count)
        assertTrue(logs.isNotEmpty())
        viewModel.onCleared()
    }

    @Test
    fun `the view model reads the configuration when it is created although its source is created later`() = runTest {
        val closing = ClosingViewModel(testDispatcher(), 3.seconds)
        Emanate.initForTest(EmaConfiguration(destroyedTimeout = 2.seconds))
        try {
            // The source is created now, with the configuration of the view model: 5 seconds
            closing.onCreated(null)

            closing.onCleared()
            advanceUntilIdle()

            assertEquals(listOf("destroyed"), closing.finished)
        } finally {
            Emanate.reset()
        }
    }

    @Test
    fun `the side effects of destroyed are cancelled after 5 seconds`() = runTest {
        val closing = ClosingViewModel(testDispatcher(), 10.seconds)
        closing.onCreated(null)

        closing.onCleared()
        advanceUntilIdle()

        assertTrue(closing.finished.isEmpty())
    }

    @Test
    fun `destroyed is processed and then the view model ignores new actions`() = runTest {
        val viewModel = TestViewModel(testDispatcher())
        viewModel.dispatch(TestAction.Increment)
        runCurrent()

        viewModel.onCleared()
        viewModel.dispatch(TestAction.Increment)
        runCurrent()

        assertTrue("lifecycle Destroyed" in viewModel.received)
        assertEquals(1, viewModel.stateFlow.value.count)
    }

    @Test
    fun `the state is rendered on initialization by default`() = runTest {
        val viewModel = TestViewModel(testDispatcher())

        assertTrue(viewModel.shouldRenderState)
    }

    @Test
    fun `without updateOnInitialization the state is rendered after the first action`() = runTest {
        val viewModel = TestViewModel(testDispatcher(), renderOnInitialization = false)

        viewModel.onCreated()
        viewModel.onResumeView()
        runCurrent()
        assertFalse(viewModel.shouldRenderState)

        viewModel.dispatch(TestAction.Increment)
        runCurrent()
        assertTrue(viewModel.shouldRenderState)
        viewModel.onCleared()
    }

    @Test
    fun `the logger logs the actions with the name of the view model`() = runTest {
        val logs = mutableListOf<Pair<String, String>>()
        val viewModel = TestViewModel(testDispatcher(), logger = EmaLogger { tag, message -> logs += tag to message })

        viewModel.dispatch(TestAction.Increment)
        runCurrent()

        assertEquals(setOf("TestViewModel"), logs.map { it.first }.toSet())
        assertTrue(logs.any { "Increment" in it.second })
        assertTrue(logs.any { "count=1" in it.second })
        viewModel.onCleared()
    }

    @Test
    fun `the prefix of the logger is added to the tag`() = runTest {
        val tags = mutableSetOf<String>()
        val viewModel = TestViewModel(testDispatcher(), logger = EmaLogger(prefix = "EMA_") { tag, _ -> tags += tag })

        viewModel.dispatch(TestAction.Increment)
        runCurrent()

        assertEquals(setOf("EMA_TestViewModel"), tags)
        viewModel.onCleared()
    }

    @Test
    fun `the logger logs the received action and the state after a middleware continues it later`() = runTest {
        val logs = mutableListOf<String>()
        val viewModel = TestViewModel(testDispatcher(), logger = EmaLogger { _, message -> logs += message })

        viewModel.dispatch(TestAction.Slow)
        runCurrent()
        // The delay of Slow hasn't finished, so only the received action is logged
        assertTrue("Received action: Slow" in logs)
        assertTrue(logs.none { "State after apply action" in it })

        advanceTimeBy(1500)
        runCurrent()
        val stateAfter = logs.dropWhile { "State after apply action" !in it }
        assertTrue(stateAfter.any { "count=1" in it })
        viewModel.onCleared()
    }

    @Test
    fun `view models receive the actions broadcast by other view models`() = runTest {
        val receiver = TestViewModel(testDispatcher())
        val sender = SenderViewModel(testDispatcher())
        receiver.onCreated()
        sender.onCreated()

        sender.dispatch(TestAction.Update("result"))
        runCurrent()

        assertEquals("result", receiver.stateFlow.value.text)
        sender.onCleared()
        receiver.onCleared()
    }

    @Test
    fun `a cleared view model doesn't receive broadcast actions`() = runTest {
        val receiver = TestViewModel(testDispatcher())
        val sender = SenderViewModel(testDispatcher())
        receiver.onCreated()
        sender.onCreated()

        receiver.onCleared()
        sender.dispatch(TestAction.Update("result"))
        runCurrent()

        assertEquals("", receiver.stateFlow.value.text)
        sender.onCleared()
    }
}
