package com.carmabs.emanate.core.source

import com.carmabs.emanate.core.Emanate
import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.error.EmaError
import com.carmabs.emanate.core.error.EmaErrorPolicy
import com.carmabs.emanate.core.logging.EmaLogger
import com.carmabs.emanate.core.model.EmaConfiguration
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

private data class CountState(val count: Int = 0) : EmaState

private sealed interface CountAction : EmaAction {
    data object Increment : CountAction
    data object BrokenReducer : CountAction
    data object BrokenMiddleware : CountAction
}

@OptIn(ExperimentalCoroutinesApi::class)
class EmaSourceErrorTest {

    private val errors = mutableListOf<EmaError>()
    private val logs = mutableListOf<String>()

    @BeforeTest
    fun setUp() {
        Emanate.initForTest(
            EmaConfiguration(
                errorPolicy = EmaErrorPolicy.Catch { errors += it },
                logger = EmaLogger { tag, message -> logs += "$tag: $message" }
            )
        )
    }

    @AfterTest
    fun tearDown() {
        Emanate.reset()
    }

    private fun TestScope.countSource() = EmaSource<CountState, CountAction, EmaEvent.EMPTY>(
        CountState(),
        UnconfinedTestDispatcher(testScheduler)
    ) {
        middlewares {
            add<CountAction.BrokenMiddleware> { error("broken middleware") }
        }
        reducers {
            add<CountAction> { action ->
                when (action) {
                    CountAction.Increment -> state.copy(count = state.count + 1)
                    CountAction.BrokenReducer -> error("broken reducer")
                    CountAction.BrokenMiddleware -> state.copy(count = 100)
                }
            }
        }
    }

    @Test
    fun `a reducer that fails discards its action and the source keeps working`() = runTest {
        val source = countSource()

        source.dispatch(CountAction.BrokenReducer)
        source.dispatch(CountAction.Increment)
        runCurrent()

        assertEquals(CountState(count = 1), source.state)
        assertEquals("action BrokenReducer", errors.single().reflection.methodName)
        assertEquals("broken reducer", errors.single().exception.message)
        source.close()
    }

    @Test
    fun `a middleware that fails discards its action and the source keeps working`() = runTest {
        val source = countSource()

        source.dispatch(CountAction.BrokenMiddleware)
        source.dispatch(CountAction.Increment)
        runCurrent()

        // The reducer of BrokenMiddleware doesn't run
        assertEquals(CountState(count = 1), source.state)
        assertEquals("action BrokenMiddleware", errors.single().reflection.methodName)
        source.close()
    }

    @Test
    fun `the caught errors are logged with the action that failed`() = runTest {
        val source = countSource()

        source.dispatch(CountAction.BrokenReducer)
        runCurrent()

        assertTrue(logs.any { it.startsWith("EmaSource: action BrokenReducer failed: ") && "broken reducer" in it })
        source.close()
    }

    @Test
    fun `the source keeps the configuration it had when it was created`() = runTest {
        val source = countSource()
        Emanate.initForTest(EmaConfiguration(errorPolicy = EmaErrorPolicy.Throw, logger = null))

        source.dispatch(CountAction.BrokenReducer)
        runCurrent()

        // Caught with the policy of setUp instead of thrown
        assertEquals("action BrokenReducer", errors.single().reflection.methodName)
        source.close()
    }
}
