package com.carmabs.emanate.core.viewmodel

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

private data class CountState(val count: Int = 0) : EmaState

private data object IncrementAction : EmaAction

/**
 * View model that doesn't pass a logger, to check the default one
 */
private class DefaultLoggerViewModel(dispatcher: CoroutineDispatcher) :
    EmaViewModel<CountState, IncrementAction, EmaEvent.EMPTY>(CountState(), dispatcher) {

    override fun setup() = source {
        reducers {
            add<IncrementAction> { state.copy(count = state.count + 1) }
        }
    }
}

/**
 * The default logger writes to the standard output, which is only captured in the JVM
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EmaViewModelLoggerTest {

    @Test
    fun `the view model logs with println by default`() = runTest {
        val output = ByteArrayOutputStream()
        val systemOut = System.out
        System.setOut(PrintStream(output))
        try {
            val viewModel = DefaultLoggerViewModel(UnconfinedTestDispatcher(testScheduler))
            viewModel.dispatch(IncrementAction)
            runCurrent()
            viewModel.onCleared()
        } finally {
            System.setOut(systemOut)
        }

        assertTrue(output.toString().lines().any { it.startsWith("EMA_LOG_DefaultLoggerViewModel: ") })
    }
}
