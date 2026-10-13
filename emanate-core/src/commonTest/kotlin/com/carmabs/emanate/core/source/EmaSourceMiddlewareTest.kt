package com.carmabs.emanate.core.source

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.source.middleware.middlewareDelayOf
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

private data class TestState(val count: Int = 0, val text: String = "") : EmaState

private sealed interface TestAction : EmaAction {
    data object Increment : TestAction
    data class Update(val text: String) : TestAction
    data object Blocked : TestAction
    data object Replaced : TestAction
    data object Slow : TestAction
    data object NextTwice : TestAction
    data object NextLater : TestAction
    data class Search(val query: String) : TestAction
    data class Guarded(val refresh: Refresh) : TestAction
}

private enum class Refresh { OK, FAIL, FAIL_CAUGHT, HANG }

private sealed interface TestEvent : EmaEvent

@OptIn(ExperimentalCoroutinesApi::class)
class EmaSourceMiddlewareTest {

    private val received = mutableListOf<String>()

    private fun TestScope.testSource() = EmaSource<TestState, TestAction, TestEvent>(
        TestState(),
        dispatcher = UnconfinedTestDispatcher(testScheduler)
    ) {
        middlewares {
            add<TestAction.Blocked> { received += "blocked" }
            add<TestAction.Replaced> { next(TestAction.Update("replaced")) }
            +middlewareDelayOf(TestAction.Slow::class, 1.seconds)
            add<TestAction.NextTwice> {
                next()
                received += runCatching { next() }.exceptionOrNull()?.message.orEmpty()
            }
            add<TestAction.NextLater> {
                val middlewareScope = this
                nextAfter { received += runCatching { middlewareScope.state }.exceptionOrNull()?.message.orEmpty() }
            }
            add<TestAction.Search> { action ->
                nextAfter(id = "search") {
                    delay(500)
                    received += "search ${action.query}"
                }
            }
            add<TestAction.Guarded> { action ->
                nextAfter(timeout = 2.seconds, onTimeout = { dispatch(TestAction.Update("timeout")) }) {
                    dispatch(TestAction.Update("refreshing"))
                    when (action.refresh) {
                        Refresh.OK -> delay(500)

                        Refresh.FAIL -> {
                            dispatch(TestAction.Update("expired"))
                            stop()
                        }

                        Refresh.FAIL_CAUGHT -> {
                            try {
                                stop()
                            } catch (exception: Exception) {
                                dispatch(TestAction.Update("caught"))
                            }
                        }

                        Refresh.HANG -> delay(5000)
                    }
                }
            }
            // Runs after the delay of Slow, to check that the chain continues where it stopped
            add<TestAction.Slow> {
                received += "after slow"
                next()
            }
            add<EmaAction> { action ->
                received += "last ${action::class.simpleName}"
                next()
            }
        }
        reducers {
            add<TestAction.Increment> { state.copy(count = state.count + 1) }
            add<TestAction.Blocked> { state.copy(count = state.count + 1) }
            add<TestAction.Guarded> { state.copy(count = state.count + 1) }
            add<TestAction.Slow> { state.copy(count = state.count + 1) }
            add<TestAction.Search> { action -> state.copy(text = action.query) }
            add<TestAction.Update> { action -> state.copy(text = action.text) }
        }
    }

    @Test
    fun `a middleware only receives the actions of its type`() = runTest {
        val source = testSource()

        source.dispatch(TestAction.Increment)
        runCurrent()

        assertEquals(listOf("last Increment"), received)
        assertEquals(1, source.state.count)
        source.close()
    }

    @Test
    fun `the action doesn't continue if next isn't called`() = runTest {
        val source = testSource()

        source.dispatch(TestAction.Blocked)
        runCurrent()

        assertEquals(listOf("blocked"), received)
        assertEquals(0, source.state.count)
        source.close()
    }

    @Test
    fun `next can continue with other action`() = runTest {
        val source = testSource()

        source.dispatch(TestAction.Replaced)
        runCurrent()

        assertEquals("replaced", source.state.text)
        source.close()
    }

    @Test
    fun `next can only be called once`() = runTest {
        val source = testSource()

        source.dispatch(TestAction.NextTwice)
        runCurrent()

        assertTrue("next can only be called once for each action" in received)
        source.close()
    }

    @Test
    fun `the scope of a middleware can't be used after it finishes`() = runTest {
        val source = testSource()

        source.dispatch(TestAction.NextLater)
        runCurrent()

        assertTrue(received.any { it.startsWith("state can only be called while the middleware is running") })
        source.close()
    }

    @Test
    fun `the delay middleware continues the chain later`() = runTest {
        val source = testSource()

        source.dispatch(TestAction.Slow)
        runCurrent()
        assertEquals(0, source.state.count)

        advanceTimeBy(1500)
        runCurrent()
        assertEquals(1, source.state.count)
        assertTrue("after slow" in received)
        source.close()
    }

    @Test
    fun `nextAfter with the same id cancels the previous one`() = runTest {
        val source = testSource()

        source.dispatch(TestAction.Search("a"))
        advanceTimeBy(100)
        source.dispatch(TestAction.Search("ab"))
        advanceTimeBy(1000)
        runCurrent()

        assertEquals(listOf("search ab"), received.filter { it.startsWith("search") })
        assertEquals("ab", source.state.text)
        source.close()
    }

    @Test
    fun `nextAfter continues the chain when it finishes`() = runTest {
        val source = testSource()

        source.dispatch(TestAction.Guarded(Refresh.OK))
        runCurrent()
        // Actions dispatched while waiting arrive, but the guarded one doesn't continue yet
        assertEquals(TestState(count = 0, text = "refreshing"), source.state)

        advanceTimeBy(1000)
        runCurrent()
        assertEquals(1, source.state.count)
        source.close()
    }

    @Test
    fun `nextAfter doesn't continue when stop is called`() = runTest {
        val source = testSource()

        source.dispatch(TestAction.Guarded(Refresh.FAIL))
        advanceTimeBy(1000)
        runCurrent()

        assertEquals(TestState(count = 0, text = "expired"), source.state)
        source.close()
    }

    @Test
    fun `nextAfter doesn't continue when stop is called and its exception is caught`() = runTest {
        val source = testSource()

        source.dispatch(TestAction.Guarded(Refresh.FAIL_CAUGHT))
        advanceTimeBy(1000)
        runCurrent()

        assertEquals(TestState(count = 0, text = "caught"), source.state)
        source.close()
    }

    @Test
    fun `nextAfter doesn't continue when it times out`() = runTest {
        val source = testSource()

        source.dispatch(TestAction.Guarded(Refresh.HANG))
        advanceTimeBy(6000)
        runCurrent()

        assertEquals(TestState(count = 0, text = "timeout"), source.state)
        source.close()
    }

    @Test
    fun `closing the source cancels the work of the middlewares`() = runTest {
        val source = testSource()

        source.dispatch(TestAction.Slow)
        source.close()
        advanceTimeBy(1500)
        runCurrent()

        assertEquals(0, source.state.count)
    }
}
