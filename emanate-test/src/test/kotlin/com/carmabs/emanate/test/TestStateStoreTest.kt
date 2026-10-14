package com.carmabs.emanate.test

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.initializer.EmaInitializer
import com.carmabs.emanate.core.savestate.EmaRestoreState
import com.carmabs.emanate.core.savestate.backupOf
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import com.carmabs.emanate.core.viewmodel.EmaViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

private data class SearchState(val query: String = "", val results: List<String> = emptyList()) : EmaState

private sealed interface SearchAction : EmaAction {
    data class QueryWritten(val query: String) : SearchAction
}

private class SearchViewModel :
    EmaViewModel<SearchState, SearchAction, EmaEvent.EMPTY>(
        initialDataState = SearchState(),
        logger = null
    ) {
    override fun setup() = source {
        install(
            backupOf {
                add("query", save = { query }, restore = { copy(query = it) })
            }
        )
        reducers {
            add<SearchAction.QueryWritten> { action -> state.copy(query = action.query) }
            add<EmaRestoreState> { state.copy(results = listOf("restored")) }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TestStateStoreTest {

    @BeforeTest
    fun setUp() {
        useTestDispatchers()
    }

    @AfterTest
    fun tearDown() {
        resetTestDispatchers()
    }

    @Test
    fun `the store keeps the values saved by the view model`() = runTest {
        val store = testStateStore()
        val viewModel = SearchViewModel().withStateStore(store)

        viewModel.onCreated(null)
        viewModel.dispatch(SearchAction.QueryWritten("ema"))
        advanceUntilIdle()

        assertEquals(mapOf("query" to "ema"), store.values)
        viewModel.onCleared()
    }

    @Test
    fun `a new view model with the same store restores the state as after the death of the process`() = runTest {
        val store = testStateStore()
        val first = SearchViewModel().withStateStore(store)
        first.onCreated(null)
        first.dispatch(SearchAction.QueryWritten("ema"))
        advanceUntilIdle()
        first.onCleared()

        val second = SearchViewModel().withStateStore(store)
        val recorder = second.record()
        second.onCreated(null)
        advanceUntilIdle()

        recorder.assertActions(EmaInitializer.EMPTY, restoreStateOf("query" to "ema"))
        recorder.assertState(SearchState(query = "ema", results = listOf("restored")))
        second.onCleared()
    }

    @Test
    fun `a reducer of the restore of the saved state runs`() {
        SearchViewModel().assertSetup()
    }

    @Test
    fun `a reducer of the restore never runs without a backup`() {
        val viewModel = object : EmaViewModel<SearchState, SearchAction, EmaEvent.EMPTY>(SearchState(), logger = null) {
            override fun setup() = source {
                reducers {
                    add<EmaRestoreState> { state.copy(results = listOf("restored")) }
                }
            }
        }

        assertFailsWith<AssertionError> { viewModel.assertSetup() }
    }

    @Test
    fun `a null value removes the saved one`() {
        val store = testStateStore()
        store.write("query", "ema")

        store.write("query", null)

        assertTrue(store.values.isEmpty())
    }

    @Test
    fun `the store must be attached before the view is created`() = runTest {
        val viewModel = SearchViewModel()
        viewModel.onCreated(null)

        assertFailsWith<IllegalStateException> { viewModel.withStateStore(testStateStore()) }
        viewModel.onCleared()
    }
}
