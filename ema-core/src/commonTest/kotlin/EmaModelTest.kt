import com.carmabs.ema.core.Ema
import com.carmabs.ema.core.action.DefaultEmaActionDispatcher
import com.carmabs.ema.core.action.EmaAction
import com.carmabs.ema.core.action.EmaActionDispatcherEmpty
import com.carmabs.ema.core.broadcast.EmaBroadcastEvent
import com.carmabs.ema.core.broadcast.EmaFlowBroadcastManager
import com.carmabs.ema.core.delegate.emaBooleanDelegate
import com.carmabs.ema.core.delegate.emaStartTriggerDelegate
import com.carmabs.ema.core.delegate.emaSyncDelegate
import com.carmabs.ema.core.dialog.EmaDialogListener
import com.carmabs.ema.core.extension.concat
import com.carmabs.ema.core.extension.minDelay
import com.carmabs.ema.core.extension.suspendCoroutineWithTimeout
import com.carmabs.ema.core.extension.until
import com.carmabs.ema.core.extension.untilInstanceOf
import com.carmabs.ema.core.extension.whileIsInstanceOf
import com.carmabs.ema.core.logging.EmaDataClassPrinter
import com.carmabs.ema.core.logging.toStringPretty
import com.carmabs.ema.core.manager.PermissionState
import com.carmabs.ema.core.model.EmaConfiguration
import com.carmabs.ema.core.model.EmaImage
import com.carmabs.ema.core.model.EmaMethodNameResolver
import com.carmabs.ema.core.model.EmaMultiplePermissionRequest
import com.carmabs.ema.core.model.EmaPermissionRequest
import com.carmabs.ema.core.model.EmaResult
import com.carmabs.ema.core.model.emaFlowSingleEvent
import com.carmabs.ema.core.model.flatMap
import com.carmabs.ema.core.model.flatMapError
import com.carmabs.ema.core.model.map
import com.carmabs.ema.core.model.mapError
import com.carmabs.ema.core.model.mapResult
import com.carmabs.ema.core.model.onFailure
import com.carmabs.ema.core.model.onSuccess
import com.carmabs.ema.core.navigator.EmaNavigationNode
import com.carmabs.ema.core.selector.EmaUniqueSelector
import com.carmabs.ema.core.state.EmaEvent
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.usecase.EmaFlowUseCase
import com.carmabs.ema.core.usecase.EmaSyncUseCase
import com.carmabs.ema.core.usecase.EmaUseCase
import com.carmabs.ema.core.value.EmaUriRes
import com.carmabs.ema.core.value.EmaUriType
import com.carmabs.ema.core.viewmodel.EmaViewModel
import kotlin.coroutines.resume
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield

class EmaResultTest {

    private val success: EmaResult<Int, String> = EmaResult.success(2)
    private val failure: EmaResult<Int, String> = EmaResult.failure("error")

    @Test
    fun `reading values`() {
        assertTrue(success.isSuccess)
        assertFalse(success.isFailure)
        assertEquals(2, success.getOrNull())
        assertEquals(2, success.getOrDefault(0))
        assertEquals(2, success.getOrThrow())
        assertNull(success.getFailureOrNull())

        assertTrue(failure.isFailure)
        assertNull(failure.getOrNull())
        assertEquals(0, failure.getOrDefault(0))
        assertEquals("error", failure.getFailureOrNull())
        assertFailsWith<IllegalArgumentException> { failure.getOrThrow() }
        assertFailsWith<IllegalStateException> {
            EmaResult.failure<Int, Throwable>(IllegalStateException()).getOrThrow()
        }
    }

    @Test
    fun `transformations`() {
        assertEquals(4, success.map { it * 2 }.getOrNull())
        assertEquals("error", failure.map { it * 2 }.getFailureOrNull())
        assertEquals(6, success.flatMap { EmaResult.success<Int, String>(it * 3) }.getOrNull())
        assertEquals("error", failure.flatMap { EmaResult.success<Int, String>(it) }.getFailureOrNull())
        assertEquals(5, failure.mapError { it.length }.getFailureOrNull())
        assertEquals(2, success.mapError { it.length }.getOrNull())
        assertEquals(1, failure.flatMapError { EmaResult.success<Int, Int>(1) }.getOrNull())
        assertEquals(2, success.flatMapError { EmaResult.failure<Int, Int>(1) }.getOrNull())
        assertEquals(
            "2",
            success.mapResult {
                EmaResult.success<String, String>(it.getOrNull().toString())
            }.getOrNull()
        )
    }

    @Test
    fun `side effects return the same result`() {
        var value = 0
        var error = ""
        assertSame(success, success.onSuccess { value = it }.onFailure { error = it })
        failure.onSuccess { value = -1 }.onFailure { error = it }
        assertEquals(2, value)
        assertEquals("error", error)
    }
}

class EmaModelTest {

    @AfterTest
    fun tearDown() {
        Ema.reset()
    }

    @Test
    fun `Ema uses the default configuration until it is initialized once`() {
        assertSame(Ema.configuration, Ema.configuration)
        assertSame(EmaDataClassPrinter.ToString, Ema.configuration.dataClassPrinter)
        val configuration = EmaConfiguration(dataClassPrinter = { "custom" })
        Ema.init(configuration)
        assertSame(configuration, Ema.configuration)
        assertFailsWith<IllegalStateException> { Ema.init(EmaConfiguration()) }
    }

    @Test
    fun `objects are printed with the configured printer`() {
        assertEquals("CounterState(count=1, text=)", CounterState(count = 1).toStringPretty())
        assertEquals("null", EmaDataClassPrinter.ToString.print(null))
        Ema.init(EmaConfiguration(dataClassPrinter = { "printed $it" }))
        assertEquals("printed 1", 1.toStringPretty())
    }

    @Test
    fun `the default method name resolver names the container class`() {
        assertEquals("Method in CounterState", EmaMethodNameResolver.Default.resolve(CounterState(), {}))
    }

    @Test
    fun `action types and empty values`() {
        assertEquals("EmaAction", EmaAction.type)
        assertEquals("EmaAction", CounterAction.Increment.type)
        assertEquals("Initializer", CounterInitializer(1).type)
        assertEquals("Lifecycle", EmaAction.Lifecycle.Started.type)
        assertEquals("EmaAction", EmaAction.EMPTY.type)
        assertEquals("EmaInitializer", EmaAction.Initializer.KEY)
        assertEquals("Initializer", EmaAction.Initializer.EMPTY.type)
        assertTrue(EmaState.EMPTY is EmaState)
        assertTrue(EmaEvent.EMPTY is EmaEvent)
    }

    @Test
    fun `action dispatchers`() {
        val received = mutableListOf<CounterAction>()
        DefaultEmaActionDispatcher<CounterAction> { received.add(it) }.dispatch(CounterAction.Increment)
        EmaActionDispatcherEmpty<CounterAction>().dispatch(CounterAction.Increment)
        assertEquals(listOf<CounterAction>(CounterAction.Increment), received)
    }

    @Test
    fun `empty ViewModel does nothing`() = runTest {
        val viewModel = EmaViewModel.EMPTY
        viewModel.setScope(CoroutineScope(Dispatchers.Default))
        viewModel.onCreated()
        viewModel.onStartView()
        viewModel.onResumeView()
        viewModel.onPauseView()
        viewModel.onStopView()
        viewModel.onCleared()
        viewModel.consumeEvent(EmaEvent.EMPTY)
        assertEquals(EmaState.EMPTY, viewModel.initialState)
        assertEquals(EmaState.EMPTY, viewModel.stateFlow.value)
        assertTrue(viewModel.shouldRenderState)
        assertEquals(emptyList(), viewModel.eventFlow.toList())
    }

    @Test
    fun `images compare their content`() {
        assertEquals(EmaImage.ByteArray(byteArrayOf(1, 2)), EmaImage.ByteArray(byteArrayOf(1, 2)))
        assertEquals(EmaImage.ByteArray(byteArrayOf(1, 2)).hashCode(), EmaImage.ByteArray(byteArrayOf(1, 2)).hashCode())
        assertNotEquals(EmaImage.ByteArray(byteArrayOf(1)), EmaImage.ByteArray(byteArrayOf(2)))
        assertNotEquals<Any>(EmaImage.ByteArray(byteArrayOf(1)), "image")
        assertEquals(EmaImage.Id(1, width = 2), EmaImage.Id(1, width = 2))
        val uri = EmaImage.Uri(EmaUriRes("a", EmaUriType.Drawable), height = 3, colorTint = 4)
        assertEquals(3, uri.height)
        assertEquals(4, uri.colorTint)
        assertNull(uri.width)
    }

    @Test
    fun `permission requests`() {
        var single: PermissionState? = null
        var multiple: Map<String, PermissionState>? = null
        val request = EmaPermissionRequest.createRequest { single = it }
        val multipleRequest = EmaMultiplePermissionRequest.createRequest { multiple = it }
        assertTrue(request.shouldRequest)
        assertTrue(multipleRequest.shouldRequest)
        request.onPermissionResponse(PermissionState.GRANTED)
        multipleRequest.onPermissionResponse(mapOf("a" to PermissionState.NOT_GRANTED))
        assertEquals(PermissionState.GRANTED, single)
        assertEquals(mapOf("a" to PermissionState.NOT_GRANTED), multiple)

        assertFalse(EmaPermissionRequest.cancelRequest().shouldRequest)
        assertFalse(EmaMultiplePermissionRequest.cancelRequest().shouldRequest)
        EmaPermissionRequest.cancelRequest().onPermissionResponse(PermissionState.GRANTED)
        EmaMultiplePermissionRequest.cancelRequest().onPermissionResponse(emptyMap())
    }

    @Test
    fun `dialog listener outside press defaults to back`() {
        var backs = 0
        val listener = object : EmaDialogListener {
            override fun onBackPressed() {
                backs++
            }
        }
        listener.onOutsidePressed()
        listener.onDestroyed()
        assertEquals(1, backs)
    }

    @Test
    fun `unique selector selects one option and deselects the rest`() {
        val selected = mutableListOf<String>()
        val unselected = mutableListOf<String>()
        EmaUniqueSelector(
            listOf("a", "b", "c"),
            object : EmaUniqueSelector.OnSelectionListener<String> {
                override fun onSelected(option: String) {
                    selected.add(option)
                }

                override fun onUnselected(option: String) {
                    unselected.add(option)
                }
            }
        ).select("b")
        assertEquals(listOf("b"), selected)
        assertEquals(listOf("a", "c"), unselected)
    }

    private class Delegates {
        var flag by emaBooleanDelegate<Delegates>(false)
        var value by emaSyncDelegate("a")
        val trigger by emaStartTriggerDelegate()
    }

    @Test
    fun `delegates store their values`() {
        val delegates = Delegates()
        assertFalse(delegates.flag)
        delegates.flag = true
        assertTrue(delegates.flag)
        assertEquals("a", delegates.value)
        delegates.value = "b"
        assertEquals("b", delegates.value)
        assertSame(delegates.trigger, delegates.trigger)
        assertFalse(delegates.trigger.hasBeenStarted)
    }

    @Test
    fun `navigation nodes keep the history`() {
        val first = EmaNavigationNode("a")
        val second = first.next("b")
        val third = second.next("c")
        assertEquals("b", third.back()?.value)
        assertTrue(third.hasPreviousNode(first))
        assertFalse(first.hasPreviousNode(third))
        assertTrue(third.hasPreviousNodeValue("a"))
        assertFalse(third.hasPreviousNodeValue("z"))
        assertNull(first.previous)
    }
}

private class Doubler : EmaUseCase<Int, Int>() {
    override suspend fun useCaseFunction(input: Int) = input * 2
}

private class Counter : EmaFlowUseCase<Int, Int>() {
    override fun useCaseFunction(input: Int): Flow<Int> = flowOf(*(1..input).toList().toTypedArray())
}

private class Upper : EmaSyncUseCase<String, String> {
    override fun invoke(input: String) = input.uppercase()
}

private class UserCreated(override val data: String) : EmaBroadcastEvent<String>

@OptIn(ExperimentalCoroutinesApi::class)
class EmaCoroutinesTest {

    @Test
    fun `use cases run on the given dispatcher`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        assertEquals(4, Doubler()(2, dispatcher))
        assertEquals(listOf(1, 2, 3), Counter()(3, dispatcher).toList())
        assertEquals("EMA", Upper()("ema"))
    }

    @Test
    fun `minDelay waits at least the given time`() = runTest {
        val result = minDelay(500.milliseconds) { "done" }
        assertEquals("done", result)
        // The real time spent is subtracted, so the virtual delay can be a few milliseconds shorter
        assertTrue(testScheduler.currentTime >= 400)
    }

    @Test
    fun `suspendCoroutineWithTimeout resumes or times out`() = runTest {
        assertEquals(1, suspendCoroutineWithTimeout<Int>(100.milliseconds) { it.resume(1) })
        assertFailsWith<TimeoutCancellationException> {
            suspendCoroutineWithTimeout<Int>(100.milliseconds) { }
        }
    }

    @Test
    fun `flow extensions`() = runTest {
        val values: Flow<Any> = flowOf(1, 2, "three", 4)
        assertEquals(listOf<Any>(1, 2), values.whileIsInstanceOf(Int::class).toList())
        assertEquals("three", values.untilInstanceOf(String::class))
        assertEquals(4, flowOf(1, 4, 6).until { it > 3 })
        assertEquals(listOf(1, 2, 3), flowOf(1, 2).concat(flowOf(3)).toList())
    }

    @Test
    fun `single event flow drops old values when nobody collects`() = runTest {
        val flow = emaFlowSingleEvent<Int>()
        assertTrue(flow.tryEmit(1))
        assertTrue(flow.tryEmit(2))
        assertEquals(0, flow.replayCache.size)
    }

    @Test
    fun `broadcast manager delivers events to registered listeners`() = runTest {
        val manager = EmaFlowBroadcastManager()
        val received = mutableListOf<String>()
        manager.sendBroadcastEvent(UserCreated("ignored"))
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            manager.registerBroadcast(UserCreated::class) { received.add(it) }
        }
        yield()
        manager.sendBroadcastEvent(UserCreated("ana"))
        assertEquals(listOf("ana"), received)
        job.cancel()
    }

    @Test
    fun `Ema exposes a single broadcast manager`() {
        assertSame(Ema.broadcastManager, Ema.broadcastManager)
    }

    @Test
    fun `first emitted value of a flow`() = runTest {
        assertEquals(1, flowOf(1, 2).first())
    }
}
