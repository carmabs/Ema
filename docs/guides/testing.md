# Testing

Because a ViewModel has no Android dependencies, you test it as plain Kotlin: no emulator, no Robolectric.
If your ViewModels live in a multiplatform module, the same tests run on every target from `commonTest`.
The whole state/event contract is observable:

- `dispatch(action)` is the input.
- `stateFlow.value` is the state.
- `eventFlow` holds the pending events.

Add `kotlinx-coroutines-test` to your test dependencies:

```kotlin
class CounterViewModelTest {

    private fun TestScope.createViewModel() =
        CounterViewModel(CoroutineScope(UnconfinedTestDispatcher(testScheduler))).apply {
            onCreated()
        }

    @Test
    fun `increment action updates the state`() = runTest {
        val viewModel = createViewModel()

        viewModel.dispatch(CounterAction.Increment)

        assertEquals(CounterState(count = 1), viewModel.stateFlow.value)
    }

    @Test
    fun `reaching the limit posts an event`() = runTest {
        val viewModel = createViewModel()

        repeat(10) { viewModel.dispatch(CounterAction.Increment) }

        assertEquals(
            listOf<CounterEvent>(CounterEvent.LimitReached(10)),
            viewModel.eventFlow.first()
        )
    }
}
```

The tests of the library itself, in `ema-core/src/commonTest`, have more examples: side effects, events,
results between screens and the view lifecycle.

## Things to know

1. **Pass your own scope.** The second constructor parameter of `EmaViewModelBasic` / `EmaViewModelAction` is the
   `CoroutineScope`. Expose it in the constructor of your ViewModels, with a default value, so tests can replace it.
   The default scope uses `Dispatchers.Main`, which does not exist in unit tests. With an `UnconfinedTestDispatcher`
   the work runs immediately; with a `StandardTestDispatcher`, call `advanceUntilIdle()` to run it.

   ```kotlin
   class CounterViewModel(scope: CoroutineScope = EmaMainScope()) :
       EmaViewModelAction<CounterState, CounterAction, CounterEvent>(CounterState(), scope)
   ```
2. **No configuration is needed.** If `Ema.init` is not called, the default configuration is used. Do not call
   `Ema.init` in tests: it can be called only once per process.
3. **Call `onCreated()`** to run `onStateCreated(initializer)`. Pass an initializer if the screen needs one:
   `viewModel.onCreated(MyInitializer.Default("x"))`.
4. **Fake the use cases.** They are ordinary classes: pass a fake implementation, or mock them with your mocking library.
5. **Events are consumed by the view.** In a test, read them with `eventFlow.first()` and call `consumeEvent(event)`
   to simulate the view having handled them.
6. **Time.** `delay` inside a `sideEffect` is skipped by the test dispatcher, so debounces and timeouts run instantly.

## What to test

| Test                                                 | How                                                        |
|------------------------------------------------------|------------------------------------------------------------|
| An action changes the state                          | `dispatch`, then assert `stateFlow.value`.                 |
| An action posts an event                             | `dispatch`, then assert `eventFlow.first()`.               |
| A failure shows the right dialog / error             | Fake use case that fails, assert the field of the state.   |
| The initializer is applied                           | `onCreated(initializer)`, assert the state.                |
| The state's derived properties                       | Plain unit tests on the data class.                        |

## Testing screens

Compose screen contents are stateless: `onState(state, actions)` only draws what it receives. Draw it with any state
(see [Previews](../compose/screens.md#previews)) and verify it with Compose UI tests or screenshot tests, without a ViewModel.
