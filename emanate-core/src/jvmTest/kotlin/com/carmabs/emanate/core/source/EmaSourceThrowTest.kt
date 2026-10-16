package com.carmabs.emanate.core.source

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.configurationOn
import com.carmabs.emanate.core.error.EmaErrorPolicy
import com.carmabs.emanate.core.model.EmaConfiguration
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

private data class ThrowState(val count: Int = 0) : EmaState

private data object BrokenAction : EmaAction

private data object SlowAction : EmaAction

/**
 * runTest rethrows the exceptions that no coroutine catches when it finishes, which only can be checked in the JVM
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EmaSourceThrowTest {

    private val throwErrors = EmaConfiguration(errorPolicy = EmaErrorPolicy.Throw, logger = null)

    @Test
    fun `the Throw policy throws the exception of a reducer again`() {
        val exception = assertFailsWith<IllegalStateException> {
            runTest {
                val source = EmaSource<ThrowState, BrokenAction, EmaEvent.EMPTY>(
                    ThrowState(),
                    configurationOn(UnconfinedTestDispatcher(testScheduler), throwErrors)
                ) {
                    reducers {
                        add<BrokenAction> { error("broken reducer") }
                    }
                }
                source.dispatch(BrokenAction)
                runCurrent()
            }
        }

        assertEquals("broken reducer", exception.message)
    }

    @Test
    fun `a source stopped by an exception with the Throw policy cancels its work`() {
        var finished = false

        assertFailsWith<IllegalStateException> {
            runTest {
                val source = EmaSource<ThrowState, EmaAction, EmaEvent.EMPTY>(
                    ThrowState(),
                    configurationOn(UnconfinedTestDispatcher(testScheduler), throwErrors)
                ) {
                    reducers {
                        add<BrokenAction> { error("broken reducer") }
                    }
                    sideEffects {
                        add<SlowAction> {
                            delay(10.seconds)
                            finished = true
                        }
                    }
                }
                source.dispatch(SlowAction)
                source.dispatch(BrokenAction)
                advanceTimeBy(20.seconds)
                runCurrent()
            }
        }

        assertFalse(finished)
    }
}
