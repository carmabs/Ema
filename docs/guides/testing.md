# Testing

Because a ViewModel has no Android dependencies, you test it as plain Kotlin on the JVM: no emulator, no Robolectric.
The whole state/event contract is observable:

- `dispatch(action)` is the input.
- `stateFlow.value` is the state.
- `eventFlow` holds the pending events.

```kotlin
class EmaViewModelTest {

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

        repeat(2) { viewModel.dispatch(CounterAction.Increment) }

        assertEquals(
            listOf<CounterEvent>(CounterEvent.LimitReached(2)),
            viewModel.eventFlow.first()
        )
    }
}
```

The complete, runnable version is in `ema-core/src/test/kotlin/EmaViewModelTest.kt`.

## Things to know

1. **Initialize the configuration.** The ViewModel reads `EmaApplicationConfigProvider` when it is created. Call
   `EmaApplicationConfigProvider.init(EmaApplicationConfig())` once before creating one. It can only be initialized
   once per JVM, so wrap it in `runCatching` if several test classes do it.
2. **Pass your own scope.** The second constructor parameter of `EmaViewModelBasic` / `EmaViewModelAction` is the
   `CoroutineScope`. The default one uses `Dispatchers.Main`, which does not exist in unit tests. Use
   `CoroutineScope(Dispatchers.Unconfined)` so the work runs immediately, or a `TestScope` from `kotlinx-coroutines-test`.
3. **Call `onCreated()`** to run `onStateCreated(initializer)`. Pass an initializer if the screen needs one:
   `viewModel.onCreated(MyInitializer.Default("x"))`.
4. **Mock use cases.** They are ordinary classes: mock them with Mockito or write a fake.
5. **Events are consumed by the view.** In a test, read them with `eventFlow.first()` and call `consumeEvent(event)`
   to simulate the view having handled them.

## What to test

| Test                                                 | How                                                        |
|------------------------------------------------------|------------------------------------------------------------|
| An action changes the state                          | `dispatch`, then assert `stateFlow.value`.                 |
| An action posts an event                             | `dispatch`, then assert `eventFlow.first()`.               |
| A failure shows the right dialog / error             | Fake use case that fails, assert the field of the state.   |
| The initializer is applied                           | `onCreated(initializer)`, assert the state.                |
| The state's derived properties                       | Plain unit tests on the data class.                        |

## Testing screens

Screen contents are stateless: `onState(state, actions)` only draws what it receives. You can draw it with any state
(see [Previews](compose.md#previews)) and verify it with screenshot or UI tests, without a ViewModel.

## Test helpers

The repository contains `ema-testing-core` (an `EmaTest` base class with Mockito and `kotlinx-coroutines-test`) and
`ema-testing-android`. They are not published; copy what is useful into your own test sources.
