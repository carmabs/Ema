package com.carmabs.emanate.core.viewmodel

import com.carmabs.emanate.core.Emanate
import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.configurationOn
import com.carmabs.emanate.core.logging.EmaLogger
import com.carmabs.emanate.core.model.EmaConfiguration
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

private data class CountState(val count: Int = 0) : EmaState

private data object IncrementAction : EmaAction

/**
 * View model that doesn't pass a logger, to check the one of the configuration
 */
private class DefaultLoggerViewModel(dispatcher: CoroutineDispatcher) :
    EmaViewModel<CountState, IncrementAction, EmaEvent.EMPTY>(CountState(), configurationOn(dispatcher)) {

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

    @AfterTest
    fun resetConfiguration() {
        Emanate.reset()
    }

    @Test
    fun `the view model logs with println by default`() = runTest {
        val output = ByteArrayOutputStream()
        val systemOut = System.out
        System.setOut(PrintStream(output))
        try {
            val viewModel = DefaultLoggerViewModel(UnconfinedTestDispatcher(testScheduler))
            viewModel.dispatch(IncrementAction)
            runCurrent()
            viewModel.close()
        } finally {
            System.setOut(systemOut)
        }

        assertTrue(output.toString().lines().any { it.startsWith("EMA_DefaultLoggerViewModel: ") })
    }

    @Test
    fun `the view model uses the logger of the configuration`() = runTest {
        val tags = mutableSetOf<String>()
        Emanate.initForTest(EmaConfiguration(logger = EmaLogger { tag, _ -> tags += tag }))

        val viewModel = DefaultLoggerViewModel(UnconfinedTestDispatcher(testScheduler))
        viewModel.dispatch(IncrementAction)
        runCurrent()
        viewModel.close()

        assertEquals(setOf("DefaultLoggerViewModel"), tags)
    }

    @Test
    fun `a null logger in the configuration disables the logs`() = runTest {
        val output = ByteArrayOutputStream()
        val systemOut = System.out
        System.setOut(PrintStream(output))
        Emanate.initForTest(EmaConfiguration(logger = null))
        try {
            val viewModel = DefaultLoggerViewModel(UnconfinedTestDispatcher(testScheduler))
            viewModel.dispatch(IncrementAction)
            runCurrent()
            viewModel.close()
        } finally {
            System.setOut(systemOut)
        }

        assertTrue(output.toString().lines().none { it.startsWith("EMA_") })
    }
}
