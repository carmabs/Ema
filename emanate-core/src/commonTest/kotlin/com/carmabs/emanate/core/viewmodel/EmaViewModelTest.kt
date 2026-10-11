package com.carmabs.emanate.core.viewmodel

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.logging.EmaLogger
import com.carmabs.emanate.core.source.SourceSetupScope
import com.carmabs.emanate.core.source.middleware.middlewareDelayOf
import com.carmabs.emanate.core.source.reducer.reducerOf
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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

private data class TestInitializer(val text: String) : EmaAction.Initializer

/**
 * Use case of the view model, to check that it can be used inside the scopes
 */
private class LoadUseCase {
    suspend operator fun invoke(): String {
        delay(1000)
        return "loaded"
    }
}

private class TestViewModel(
    dispatcher: CoroutineDispatcher,
    private val loadUseCase: LoadUseCase = LoadUseCase(),
    logger: EmaLogger? = null,
    private val renderOnInitialization: Boolean = true
) : EmaViewModel<TestState, TestAction, TestEvent>(TestState(), dispatcher, logger) {

    val received = mutableListOf<String>()

    override val updateOnInitialization: Boolean
        get() = renderOnInitialization

    /**
     * Reducer declared as a property of the view model
     */
    private val decrementReducer = reducerOf(TestAction.Decrement::class) { state.copy(count = state.count - 1) }

    override fun SourceSetupScope<TestState, TestAction, TestEvent>.setup() {
        addMiddleware<EmaAction.Lifecycle> { action ->
            received += "lifecycle ${action::class.simpleName}"
            next()
        }
        addMiddleware<EmaAction.Initializer> { action ->
            received += "initializer $action"
            next()
        }
        addMiddleware(middlewareDelayOf(TestAction.Slow::class, 1.seconds))
        addReducer<TestAction.Increment> { state.copy(count = state.count + 1) }
        addReducer(decrementReducer)
        addReducer<TestAction.Slow> { state.copy(count = state.count + 1) }
        addReducer<TestAction.Update> { action -> state.copy(text = action.text) }
        addReducer<TestInitializer> { action -> state.copy(text = action.text) }
        addReducer<EmaAction.Lifecycle.Resumed> { state.copy(resumed = true) }
        addReducer<TestAction.Load> { state.copy(text = "loading") }
        addSideEffect<TestAction.Load> {
            dispatch(TestAction.Update(loadUseCase()))
            postEvent(TestEvent.Loaded)
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

        assertEquals(listOf("initializer ${EmaAction.Initializer.EMPTY}"), viewModel.received)
        viewModel.onCleared()
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
}
