import com.carmabs.ema.core.Ema
import com.carmabs.ema.core.action.EmaAction
import com.carmabs.ema.core.broadcast.backBroadcastId
import com.carmabs.ema.core.initializer.EmaInitializer
import com.carmabs.ema.core.model.EmaConfiguration
import com.carmabs.ema.core.model.EmaMethodNameResolver
import com.carmabs.ema.core.model.EmaSideEffectConfig
import com.carmabs.ema.core.model.reflection.EmaReflection
import com.carmabs.ema.core.state.EmaEvent
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.viewmodel.EmaViewModelAction
import com.carmabs.ema.core.viewmodel.EmaViewModelBasic
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

data class CounterState(val count: Int = 0, val text: String = "") : EmaState

sealed interface CounterAction : EmaAction.Screen {
    data object Increment : CounterAction
    data class Write(val text: String) : CounterAction
}

sealed interface CounterEvent : EmaEvent {
    data class LimitReached(val limit: Int) : CounterEvent
    data object Message : CounterEvent
}

data class CounterInitializer(val start: Int) : EmaInitializer

class CounterViewModel(
    scope: CoroutineScope,
    private val renderOnInitialization: Boolean = true
) : EmaViewModelAction<CounterState, CounterAction, CounterEvent>(CounterState(), scope) {

    val hooks = mutableListOf<String>()

    override val updateOnInitialization: Boolean
        get() = renderOnInitialization

    override fun onStateCreated(initializer: EmaInitializer?) {
        hooks.add("created")
        (initializer as? CounterInitializer)?.also { updateState { copy(count = it.start) } }
    }

    override fun onBroadcastListenerSetup() {
        hooks.add("broadcast")
    }

    override fun onViewStarted() {
        hooks.add("started")
    }

    override fun onViewResumed() {
        hooks.add("resumed")
    }

    override fun onViewPaused() {
        hooks.add("paused")
    }

    override fun onViewStopped() {
        hooks.add("stopped")
    }

    override fun onDestroy() {
        hooks.add("destroyed")
    }

    override fun onAction(action: CounterAction) {
        when (action) {
            CounterAction.Increment -> {
                updateState { copy(count = count + 1) }
                if (state.count == LIMIT) postEvent(CounterEvent.LimitReached(LIMIT))
            }

            is CounterAction.Write -> updateState { copy(text = action.text) }
        }
    }

    fun message(allowDuplicated: Boolean = false) = postEvent(CounterEvent.Message, allowDuplicated)

    fun <T> launch(logName: String? = null, block: suspend CoroutineScope.() -> T) =
        sideEffect(logName = logName, action = block)

    fun <T> launchSingle(id: String, block: suspend CoroutineScope.() -> T) =
        singleSideEffect(id, action = block)

    fun currentScope() = scope

    companion object {
        const val LIMIT = 2
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class EmaViewModelTest {

    @AfterTest
    fun tearDown() {
        Ema.reset()
    }

    private fun TestScope.createViewModel(renderOnInitialization: Boolean = true) =
        CounterViewModel(CoroutineScope(UnconfinedTestDispatcher(testScheduler)), renderOnInitialization)

    @Test
    fun `initial state is exposed before any update`() = runTest {
        val viewModel = createViewModel()
        assertEquals(CounterState(), viewModel.initialState)
        assertEquals(CounterState(), viewModel.stateFlow.value)
        assertTrue(viewModel.shouldRenderState)
    }

    @Test
    fun `actions update the state`() = runTest {
        val viewModel = createViewModel()
        viewModel.dispatch(CounterAction.Increment)
        viewModel.dispatch(CounterAction.Write("hello"))
        assertEquals(CounterState(count = 1, text = "hello"), viewModel.stateFlow.value)
    }

    @Test
    fun `onCreated runs the creation hooks once with the initializer`() = runTest {
        val viewModel = createViewModel()
        viewModel.onCreated(CounterInitializer(start = 5))
        viewModel.onCreated(CounterInitializer(start = 9))
        assertEquals(listOf("created", "broadcast"), viewModel.hooks)
        assertEquals(5, viewModel.stateFlow.value.count)
    }

    @Test
    fun `view lifecycle calls the matching hooks`() = runTest {
        val viewModel = createViewModel()
        viewModel.onStartView()
        viewModel.onResumeView()
        viewModel.onPauseView()
        viewModel.onStopView()
        assertEquals(listOf("started", "resumed", "paused", "stopped"), viewModel.hooks)
    }

    @Test
    fun `the state is not rendered before the first update when updateOnInitialization is false`() = runTest {
        val viewModel = createViewModel(renderOnInitialization = false)
        assertFalse(viewModel.shouldRenderState)
        viewModel.dispatch(CounterAction.Increment)
        assertTrue(viewModel.shouldRenderState)
    }

    @Test
    fun `events are queued until they are consumed`() = runTest {
        val viewModel = createViewModel()
        viewModel.dispatch(CounterAction.Increment)
        viewModel.dispatch(CounterAction.Increment)
        val event = CounterEvent.LimitReached(CounterViewModel.LIMIT)
        assertEquals(listOf<CounterEvent>(event), viewModel.eventFlow.first())

        viewModel.consumeEvent(event)
        assertNull(withTimeoutOrNull(100) { viewModel.eventFlow.first() })
    }

    @Test
    fun `equal pending events are not duplicated unless allowed`() = runTest {
        val viewModel = createViewModel()
        viewModel.message()
        viewModel.message()
        assertEquals(1, viewModel.eventFlow.first().size)
        viewModel.message(allowDuplicated = true)
        assertEquals(2, viewModel.eventFlow.first().size)
    }

    @Test
    fun `sideEffect reports success and finish`() = runTest {
        val viewModel = createViewModel()
        var success: Int? = null
        var finished = false
        viewModel.launch { 42 }
            .onSuccess { success = it }
            .onError { error("Unexpected error") }
            .onFinish { finished = true }
        assertEquals(42, success)
        assertTrue(finished)
    }

    @Test
    fun `sideEffect reports errors when exceptions are caught`() = runTest {
        val viewModel = createViewModel()
        var failure: Throwable? = null
        var finished = false
        viewModel.launch<Int> { throw IllegalStateException("boom") }
            .onError { failure = it }
            .onFinish { finished = true }
        assertIs<IllegalStateException>(failure)
        assertTrue(finished)
    }

    @Test
    fun `listeners added before the work ends are called when it ends`() = runTest {
        val viewModel = CounterViewModel(CoroutineScope(StandardTestDispatcher(testScheduler)))
        var success: Int? = null
        var failure: Throwable? = null
        var finished = false
        val handler = viewModel.launch { delay(100); 1 }
        handler.onSuccess { success = it }.onFinish { finished = true }
        viewModel.launch<Int> { delay(100); error("boom") }.onError { failure = it }
        assertNull(success)
        advanceUntilIdle()
        assertEquals(1, success)
        assertTrue(finished)
        assertIs<IllegalStateException>(failure)
    }

    @Test
    fun `singleSideEffect cancels the previous work with the same id without reporting an error`() = runTest {
        val viewModel = CounterViewModel(CoroutineScope(StandardTestDispatcher(testScheduler)))
        val results = mutableListOf<String>()
        var errors = 0
        viewModel.launchSingle("search") { delay(100); "first" }
            .onSuccess { results.add(it) }.onError { errors++ }
        viewModel.launchSingle("search") { delay(100); "second" }
            .onSuccess { results.add(it) }.onError { errors++ }
        advanceUntilIdle()
        assertEquals(listOf("second"), results)
        assertEquals(0, errors)
    }

    @Test
    fun `onCleared cancels the scope and calls onDestroy`() = runTest {
        val viewModel = createViewModel()
        viewModel.onCleared()
        assertFalse(viewModel.currentScope().coroutineContext[kotlinx.coroutines.Job]!!.isActive)
        assertEquals(listOf("destroyed"), viewModel.hooks)
    }

    @Test
    fun `the id identifies the ViewModel class`() = runTest {
        assertEquals("EmaViewModel ID: CounterViewModel", createViewModel().id)
        assertEquals("broadcast/SenderViewModel", SenderViewModel::class.backBroadcastId.id)
    }

    @Test
    fun `setScope replaces the scope of the ViewModel`() = runTest {
        val viewModel = createViewModel()
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        viewModel.setScope(scope)
        assertEquals(scope, viewModel.currentScope())
    }

    @Test
    fun `default side effect actions receive the reflection with the resolved method name`() = runTest {
        val successes = mutableListOf<EmaReflection>()
        val finishes = mutableListOf<EmaReflection>()
        val errors = mutableListOf<Throwable>()
        Ema.init(
            EmaConfiguration(
                sideEffectConfig = EmaSideEffectConfig(
                    defaultSuccessAction = { successes.add(it.reflection) },
                    defaultFinishAction = { finishes.add(it) },
                    exceptionPolicy = EmaSideEffectConfig.ExceptionPolicy.CatchExceptions { errors.add(it.exception) },
                    methodNameResolver = EmaMethodNameResolver { _, _ -> "resolved" }
                )
            )
        )
        val viewModel = createViewModel()

        viewModel.launch { 1 }
        viewModel.launch(logName = "login") { 1 }
        viewModel.launch<Int> { error("boom") }

        assertEquals(listOf("resolved", "login"), successes.map { it.methodName })
        assertEquals(listOf("resolved", "login", "resolved"), finishes.map { it.methodName })
        assertEquals("CounterViewModel", successes.first().containerClassName)
        assertEquals("CounterViewModel", successes.first().containerClassQualifiedName)
        assertEquals(1, errors.size)
    }

    @Test
    fun `the default method name names the container`() = runTest {
        var methodName: String? = null
        Ema.init(EmaConfiguration(sideEffectConfig = EmaSideEffectConfig(defaultFinishAction = { methodName = it.methodName })))
        createViewModel().launch { 1 }
        assertEquals("Method in CounterViewModel", methodName)
    }

    @Test
    fun `exceptions are rethrown with the ThrowExceptions policy`() = runTest {
        Ema.init(EmaConfiguration(sideEffectConfig = EmaSideEffectConfig(exceptionPolicy = EmaSideEffectConfig.ExceptionPolicy.ThrowExceptions)))
        var uncaught: Throwable? = null
        val scope = CoroutineScope(
            SupervisorJob() + UnconfinedTestDispatcher(testScheduler) + CoroutineExceptionHandler { _, e -> uncaught = e }
        )
        CounterViewModel(scope).launch<Int> { error("boom") }
        assertIs<IllegalStateException>(uncaught)
    }
}

class SenderViewModel(scope: CoroutineScope) : EmaViewModelBasic<CounterState, CounterEvent>(CounterState(), scope) {
    override fun onStateCreated(initializer: EmaInitializer?) = Unit
    fun send(data: Any?) = dispatchBroadcast(data)
}

class ReceiverViewModel(scope: CoroutineScope) : EmaViewModelBasic<CounterState, CounterEvent>(CounterState(), scope) {
    val received = mutableListOf<Any?>()
    override fun onStateCreated(initializer: EmaInitializer?) = Unit
    override fun onBroadcastListenerSetup() {
        registerBackBroadcastListener(SenderViewModel::class.backBroadcastId) { received.add(it) }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class EmaBackBroadcastTest {

    @Test
    fun `the result is delivered to the receiver when the sender is cleared`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val receiver = ReceiverViewModel(scope).apply { onCreated() }
        val sender = SenderViewModel(scope)

        sender.send("user")
        assertTrue(receiver.received.isEmpty())
        sender.onCleared()
        assertEquals(listOf<Any?>("user"), receiver.received)
        receiver.onCleared()
    }

    @Test
    fun `a result without receiver is delivered when the receiver registers`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        SenderViewModel(scope).apply {
            send("pending")
            onCleared()
        }
        val receiver = ReceiverViewModel(scope).apply { onCreated() }
        assertEquals(listOf<Any?>("pending"), receiver.received)
        receiver.onCleared()
    }
}
