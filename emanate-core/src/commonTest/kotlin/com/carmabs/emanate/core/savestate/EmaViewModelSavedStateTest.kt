package com.carmabs.emanate.core.savestate

import com.carmabs.emanate.core.Emanate
import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.error.EmaError
import com.carmabs.emanate.core.error.EmaErrorPolicy
import com.carmabs.emanate.core.initializer.EmaInitializer
import com.carmabs.emanate.core.model.EmaConfiguration
import com.carmabs.emanate.core.source.EmaSource
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import com.carmabs.emanate.core.viewmodel.EmaViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

private enum class Plan { FREE, PRO }

private data class FormState(
    val name: String = "",
    val age: Int = 0,
    val plan: Plan = Plan.FREE,
    val role: String = "",
    val greeting: String = ""
) : EmaState

private sealed interface FormAction : EmaAction {
    data class NameWritten(val name: String) : FormAction
    data class AgeWritten(val age: Int) : FormAction
    data class PlanChosen(val plan: Plan) : FormAction
}

private data class RoleInitializer(val role: String) : EmaInitializer

/**
 * Store that keeps the values in memory and records every write
 */
private class MemoryStore(initialValues: Map<String, String> = emptyMap()) : EmaStateStore {

    val values = initialValues.toMutableMap()

    val writes = mutableListOf<Pair<String, String?>>()

    override fun read(key: String): String? = values[key]

    override fun write(key: String, value: String?) {
        writes += key to value
        if (value == null) values -= key else values[key] = value
    }
}

/**
 * Saves an enum by its name. A name that doesn't exist anymore isn't restored.
 */
private object PlanCodec : EmaBackupCodec<Plan> {
    override fun encode(value: Plan): String = value.name

    override fun decode(value: String): Plan? = Plan.entries.find { it.name == value }
}

private object IntCodec : EmaBackupCodec<Int> {
    override fun encode(value: Int): String = value.toString()

    override fun decode(value: String): Int? = value.toIntOrNull()
}

/**
 * Saved value implemented as a class, received in the constructor of the view model
 */
private class AgeSavedValue(private val failRestore: Boolean = false) : EmaBackupValue<FormState, Int> {

    override val key: String = "age"

    override val codec: EmaBackupCodec<Int> = IntCodec

    override fun save(state: FormState): Int = state.age

    override fun restore(state: FormState, value: Int): FormState {
        if (failRestore) error("broken restore")
        return state.copy(age = value)
    }
}

private class FormViewModel(
    dispatcher: CoroutineDispatcher,
    private val ageSavedValue: EmaBackupValue<FormState, Int> = AgeSavedValue(),
    configuration: EmaConfiguration = Emanate.configuration
) : EmaViewModel<FormState, FormAction, EmaEvent.EMPTY>(FormState(), configuration, dispatcher, logger = null) {

    val received = mutableListOf<EmaAction>()

    override fun setup() = source {
        backup {
            add("name", save = { name.takeIf { it.isNotEmpty() } }, restore = { copy(name = it) })
            +ageSavedValue
            add("plan", PlanCodec, save = { plan }, restore = { copy(plan = it) })
        }
        middlewares {
            add<EmaAction> { action ->
                received += action
                next()
            }
        }
        reducers {
            add<RoleInitializer> { initializer -> state.copy(role = initializer.role) }
            add<FormAction> { action ->
                when (action) {
                    is FormAction.NameWritten -> state.copy(name = action.name)
                    is FormAction.AgeWritten -> state.copy(age = action.age)
                    is FormAction.PlanChosen -> state.copy(plan = action.plan)
                }
            }
            // Receives the restored state
            add<EmaRestoreState> { state.copy(greeting = "Welcome back ${state.name}") }
        }
    }
}

/**
 * View model that doesn't save anything
 */
private class PlainViewModel(dispatcher: CoroutineDispatcher) :
    EmaViewModel<FormState, FormAction, EmaEvent.EMPTY>(FormState(), dispatcher = dispatcher, logger = null) {

    override fun setup() = source {}
}

@OptIn(ExperimentalCoroutinesApi::class)
class EmaViewModelSavedStateTest {

    private val errors = mutableListOf<EmaError>()

    private fun TestScope.testDispatcher() = UnconfinedTestDispatcher(testScheduler)

    @Test
    fun `the saved values are written when they change`() = runTest {
        val store = MemoryStore()
        val viewModel = FormViewModel(testDispatcher())
        viewModel.attachStateStore(store)

        viewModel.onCreated(null)
        viewModel.dispatch(FormAction.NameWritten("Ana"))
        viewModel.dispatch(FormAction.NameWritten("Ana"))
        viewModel.dispatch(FormAction.AgeWritten(30))
        viewModel.dispatch(FormAction.PlanChosen(Plan.PRO))
        runCurrent()

        assertEquals(
            listOf<Pair<String, String?>>(
                "age" to "0",
                "plan" to "FREE",
                "name" to "Ana",
                "age" to "30",
                "plan" to "PRO"
            ),
            store.writes
        )
        assertEquals(mapOf("name" to "Ana", "age" to "30", "plan" to "PRO"), store.values)
        viewModel.onCleared()
    }

    @Test
    fun `a null value removes the saved one`() = runTest {
        val store = MemoryStore()
        val viewModel = FormViewModel(testDispatcher())
        viewModel.attachStateStore(store)

        viewModel.onCreated(null)
        viewModel.dispatch(FormAction.NameWritten("Ana"))
        viewModel.dispatch(FormAction.NameWritten(""))
        runCurrent()

        assertEquals(mapOf("age" to "0", "plan" to "FREE"), store.values)
        assertEquals(
            listOf<Pair<String, String?>>("name" to "Ana", "name" to null),
            store.writes.filter { it.first == "name" }
        )
        viewModel.onCleared()
    }

    @Test
    fun `the saved values are restored with an action after the initializer`() = runTest {
        val store = MemoryStore(mapOf("name" to "Ana", "age" to "30", "plan" to "PRO"))
        val viewModel = FormViewModel(testDispatcher())
        viewModel.attachStateStore(store)

        viewModel.onCreated(RoleInitializer("admin"))
        runCurrent()

        assertEquals(
            listOf(
                RoleInitializer("admin"),
                EmaRestoreState(mapOf("name" to "Ana", "age" to "30", "plan" to "PRO"))
            ),
            viewModel.received
        )
        assertEquals(
            FormState(name = "Ana", age = 30, plan = Plan.PRO, role = "admin", greeting = "Welcome back Ana"),
            viewModel.stateFlow.value
        )
        viewModel.onCleared()
    }

    @Test
    fun `the initializer doesn't overwrite the values before they are restored`() = runTest {
        val store = MemoryStore(mapOf("name" to "Ana", "age" to "30", "plan" to "PRO"))
        val viewModel = FormViewModel(testDispatcher())
        viewModel.attachStateStore(store)

        viewModel.onCreated(RoleInitializer("admin"))
        runCurrent()

        assertTrue(store.writes.isEmpty())
        assertEquals(mapOf("name" to "Ana", "age" to "30", "plan" to "PRO"), store.values)
        viewModel.onCleared()
    }

    @Test
    fun `a new view model with the same store restores the state of the previous one`() = runTest {
        val store = MemoryStore()
        val first = FormViewModel(testDispatcher())
        first.attachStateStore(store)
        first.onCreated(RoleInitializer("admin"))
        first.dispatch(FormAction.NameWritten("Ana"))
        first.dispatch(FormAction.AgeWritten(30))
        runCurrent()
        first.onCleared()

        val second = FormViewModel(testDispatcher())
        second.attachStateStore(store)
        second.onCreated(RoleInitializer("admin"))
        runCurrent()

        assertEquals(
            FormState(name = "Ana", age = 30, role = "admin", greeting = "Welcome back Ana"),
            second.stateFlow.value
        )
        second.onCleared()
    }

    @Test
    fun `a value that can't be decoded isn't restored and is saved again from the state`() = runTest {
        val store = MemoryStore(mapOf("plan" to "GOLD"))
        val viewModel = FormViewModel(testDispatcher())
        viewModel.attachStateStore(store)

        viewModel.onCreated(null)
        runCurrent()

        assertEquals(FormState(greeting = "Welcome back "), viewModel.stateFlow.value)
        assertEquals(mapOf("age" to "0", "plan" to "FREE"), store.values)
        viewModel.onCleared()
    }

    @Test
    fun `nothing is restored when the store is empty`() = runTest {
        val viewModel = FormViewModel(testDispatcher())
        viewModel.attachStateStore(MemoryStore())

        viewModel.onCreated(null)
        runCurrent()

        assertEquals(listOf<EmaAction>(EmaInitializer.EMPTY), viewModel.received)
        viewModel.onCleared()
    }

    @Test
    fun `without a store nothing is saved nor restored`() = runTest {
        val viewModel = FormViewModel(testDispatcher())

        viewModel.onCreated(null)
        viewModel.dispatch(FormAction.NameWritten("Ana"))
        runCurrent()

        assertEquals(FormState(name = "Ana"), viewModel.stateFlow.value)
        viewModel.onCleared()
    }

    @Test
    fun `a restore that fails is reported and the other values are restored`() = runTest {
        val configuration = EmaConfiguration(errorPolicy = EmaErrorPolicy.Catch { errors += it })
        val store = MemoryStore(mapOf("name" to "Ana", "age" to "30"))
        val viewModel = FormViewModel(testDispatcher(), AgeSavedValue(failRestore = true), configuration)
        viewModel.attachStateStore(store)

        viewModel.onCreated(null)
        runCurrent()

        assertEquals(FormState(name = "Ana", greeting = "Welcome back Ana"), viewModel.stateFlow.value)
        assertEquals("restore age", errors.single().reflection.methodName)
        viewModel.onCleared()
    }

    @Test
    fun `a saved value can be replaced in the constructor`() = runTest {
        val fakeAge = savedValueOf<FormState, Int>("age", IntCodec, save = { 7 }, restore = { copy(age = 99) })
        val store = MemoryStore(mapOf("age" to "30"))
        val viewModel = FormViewModel(testDispatcher(), fakeAge)
        viewModel.attachStateStore(store)

        viewModel.onCreated(null)
        runCurrent()

        assertEquals(99, viewModel.stateFlow.value.age)
        assertEquals("7", store.values["age"])
        viewModel.onCleared()
    }

    @Test
    fun `the store can only be attached once`() = runTest {
        val viewModel = FormViewModel(testDispatcher())
        viewModel.attachStateStore(MemoryStore())

        assertFailsWith<IllegalStateException> { viewModel.attachStateStore(MemoryStore()) }
        viewModel.onCleared()
    }

    @Test
    fun `the store can't be attached after the view is created`() = runTest {
        val viewModel = FormViewModel(testDispatcher())
        viewModel.onCreated(null)

        assertFailsWith<IllegalStateException> { viewModel.attachStateStore(MemoryStore()) }
        viewModel.onCleared()
    }

    @Test
    fun `a view model without saved values ignores the store`() = runTest {
        val store = MemoryStore()
        val viewModel = PlainViewModel(testDispatcher())
        viewModel.onCreated(null)

        viewModel.attachStateStore(store)
        viewModel.dispatch(FormAction.NameWritten("Ana"))
        runCurrent()

        assertTrue(store.writes.isEmpty())
        viewModel.onCleared()
    }

    @Test
    fun `a key can only be saved once`() = runTest {
        val viewModel = object : EmaViewModel<FormState, FormAction, EmaEvent.EMPTY>(
            FormState(),
            dispatcher = testDispatcher(),
            logger = null
        ) {
            override fun setup() = source {
                backup { add("name", save = { name }, restore = { copy(name = it) }) }
                backup { add("name", save = { name }, restore = { copy(name = it) }) }
            }
        }

        assertFailsWith<IllegalArgumentException> { viewModel.onCreated(null) }
    }

    @Test
    fun `a source on its own saves and restores its values with a state saver`() = runTest {
        val store = MemoryStore()
        val first = formSource(testDispatcher())
        EmaStateSaver(first, store).restore()
        first.dispatch(FormAction.NameWritten("Ana"))
        runCurrent()
        first.close()

        val second = formSource(testDispatcher())
        EmaStateSaver(second, store).restore()
        runCurrent()

        assertEquals(mapOf("name" to "Ana"), store.values)
        assertEquals(FormState(name = "Ana"), second.state)
        second.close()
    }

    @Test
    fun `the values are restored only once`() = runTest {
        val store = MemoryStore(mapOf("name" to "Ana"))
        val source = formSource(testDispatcher())
        val saver = EmaStateSaver(source, store)
        saver.restore()
        source.dispatch(FormAction.NameWritten("Luis"))

        saver.restore()
        runCurrent()

        assertEquals(FormState(name = "Luis"), source.state)
        source.close()
    }

    @Test
    fun `a source restores the values of the action without a store`() = runTest {
        val source = formSource(testDispatcher())

        source.dispatchAction(EmaRestoreState(mapOf("name" to "Ana", "unknown" to "value")))
        runCurrent()

        assertEquals(FormState(name = "Ana"), source.state)
        source.close()
    }

    private fun formSource(dispatcher: CoroutineDispatcher) =
        EmaSource<FormState, FormAction, EmaEvent.EMPTY>(FormState(), dispatcher = dispatcher) {
            backup { add("name", save = { name.takeIf { it.isNotEmpty() } }, restore = { copy(name = it) }) }
            reducers {
                add<FormAction.NameWritten> { action -> state.copy(name = action.name) }
            }
        }
}
