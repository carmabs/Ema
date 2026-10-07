import com.carmabs.ema.core.action.EmaAction
import com.carmabs.ema.core.initializer.EmaInitializer
import com.carmabs.ema.core.model.EmaApplicationConfig
import com.carmabs.ema.core.model.EmaApplicationConfigProvider
import com.carmabs.ema.core.state.EmaEvent
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.viewmodel.EmaViewModelAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test

/**
 * Shows how to test an Ema ViewModel on the JVM. It is the reference used by the testing guide in docs/.
 */
class EmaViewModelTest {

    data class CounterState(val count: Int = 0) : EmaState

    sealed interface CounterAction : EmaAction.Screen {
        data object Increment : CounterAction
    }

    sealed interface CounterEvent : EmaEvent {
        data class LimitReached(val limit: Int) : CounterEvent
    }

    class CounterViewModel(scope: CoroutineScope) :
        EmaViewModelAction<CounterState, CounterAction, CounterEvent>(CounterState(), scope) {

        override fun onStateCreated(initializer: EmaInitializer?) = Unit

        override fun onAction(action: CounterAction) {
            when (action) {
                CounterAction.Increment -> {
                    updateState { copy(count = count + 1) }
                    if (state.count == LIMIT) postEvent(CounterEvent.LimitReached(LIMIT))
                }
            }
        }

        companion object {
            const val LIMIT = 2
        }
    }

    companion object {
        @JvmStatic
        @BeforeClass
        fun setupEma() {
            // Ema configuration can be initialized only once per JVM, so tolerate repeated calls
            runCatching { EmaApplicationConfigProvider.init(EmaApplicationConfig()) }
        }
    }

    private fun createViewModel() = CounterViewModel(CoroutineScope(Dispatchers.Unconfined)).apply {
        onCreated()
    }

    @Test
    fun `increment action updates the state`() {
        val viewModel = createViewModel()

        viewModel.dispatch(CounterAction.Increment)

        assertEquals(CounterState(count = 1), viewModel.stateFlow.value)
    }

    @Test
    fun `reaching the limit posts an event`() = runBlocking {
        val viewModel = createViewModel()

        repeat(CounterViewModel.LIMIT) { viewModel.dispatch(CounterAction.Increment) }

        assertEquals(
            listOf<CounterEvent>(CounterEvent.LimitReached(CounterViewModel.LIMIT)),
            viewModel.eventFlow.first()
        )
    }

    @Test
    fun `an event is delivered once`() = runBlocking {
        val viewModel = createViewModel()
        repeat(CounterViewModel.LIMIT) { viewModel.dispatch(CounterAction.Increment) }
        val event = CounterEvent.LimitReached(CounterViewModel.LIMIT)

        viewModel.consumeEvent(event)
        viewModel.dispatch(CounterAction.Increment)

        assertEquals(3, viewModel.stateFlow.value.count)
        // Nothing pending: the view would not receive the event again
        assertEquals(true, viewModel.eventFlow.firstOrNullNow() == null)
    }

    private suspend fun <T> kotlinx.coroutines.flow.Flow<T>.firstOrNullNow(): T? =
        kotlinx.coroutines.withTimeoutOrNull(50) { first() }
}
