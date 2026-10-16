package com.carmabs.emanate.core.source

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.configurationOn
import com.carmabs.emanate.core.source.middleware.MiddlewarePosition
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

private data class TickState(val count: Int = 0) : EmaState

private sealed interface TickAction : EmaAction {
    data object Tick : TickAction
}

@OptIn(ExperimentalCoroutinesApi::class)
class EmaSourceMiddlewarePositionTest {

    private val calls = mutableListOf<String>()

    private fun TestScope.tickSource(setup: SourceSetupScope<TickState, TickAction, EmaEvent.EMPTY>.() -> Unit) =
        EmaSource<TickState, TickAction, EmaEvent.EMPTY>(
            TickState(),
            configurationOn(UnconfinedTestDispatcher(testScheduler))
        ) {
            setup()
            reducers {
                add<TickAction.Tick> {
                    calls += "reducers"
                    state.copy(count = state.count + 1)
                }
            }
        }

    @Test
    fun `the middlewares run by position wherever their block is written`() = runTest {
        val source = tickSource {
            middlewares(MiddlewarePosition.Last) {
                add<TickAction.Tick> {
                    calls += "last"
                    next()
                }
            }
            middlewares {
                add<TickAction.Tick> {
                    calls += "in place"
                    next()
                }
            }
            middlewares(MiddlewarePosition.First) {
                add<TickAction.Tick> {
                    calls += "first"
                    next()
                }
            }
        }

        source.dispatch(TickAction.Tick)
        runCurrent()

        assertEquals(listOf("first", "in place", "last", "reducers"), calls)
        source.close()
    }

    @Test
    fun `inside a position the middlewares run in the order they are added`() = runTest {
        val source = tickSource {
            middlewares(MiddlewarePosition.Last) {
                add<TickAction.Tick> {
                    calls += "last 1"
                    next()
                }
            }
            middlewares(MiddlewarePosition.Last) {
                add<TickAction.Tick> {
                    calls += "last 2"
                    next()
                }
            }
        }

        source.dispatch(TickAction.Tick)
        runCurrent()

        assertEquals(listOf("last 1", "last 2", "reducers"), calls)
        source.close()
    }

    @Test
    fun `a last middleware sees the new state of an action delayed by a middleware written after it`() = runTest {
        val seenCounts = mutableListOf<Int>()
        val source = tickSource {
            middlewares(MiddlewarePosition.Last) {
                add<TickAction.Tick> {
                    next()
                    seenCounts += state.count
                }
            }
            middlewares {
                add<TickAction.Tick> { nextAfter { delay(300.milliseconds) } }
            }
        }

        source.dispatch(TickAction.Tick)
        advanceTimeBy(299.milliseconds)
        assertTrue(seenCounts.isEmpty())
        advanceTimeBy(2.milliseconds)

        assertEquals(listOf(1), seenCounts)
        source.close()
    }
}
