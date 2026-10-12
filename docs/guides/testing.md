# Testing

Because a ViewModel has no Android dependencies, you test it as plain Kotlin: no emulator, no Robolectric.
If your ViewModels live in a multiplatform module, the same tests run on every target from `commonTest`.
The whole state/event contract is observable:

- `dispatch(action)` is the input.
- `stateFlow.value` is the state.
- `eventFlow` holds the pending events.

## Testing an EmaViewModel with emanate-test

`emanate-test` has the utilities to test the ViewModels built with `EmaViewModel` and the
[sources](../concepts/source.md). It is a JVM library, so add it to the tests of a JVM or Android module:

```kotlin
dependencies {
    testImplementation("com.github.carmabs.ema:emanate-test:$emanateVersion")
}
```

It brings `kotlinx-coroutines-test` and requires the same version of `emanate-core` it was built with.

| Utility                         | Purpose                                                                       |
|---------------------------------|-------------------------------------------------------------------------------|
| `useTestDispatchers()`          | Runs the ViewModels, sources and use cases created after it on the dispatcher of the test, also as `Dispatchers.Main`. Call it before each test. |
| `resetTestDispatchers()`        | Restores the dispatchers and the configuration of Emanate. Call it after each test. |
| `viewModel.record()`            | Records in order the actions, states and events of a ViewModel or a source.  |
| `assertActions(...)`            | The recorded actions, including the ones blocked by a middleware.            |
| `assertStates(...)`             | Every state returned by the reducers.                                         |
| `assertState(state)` / `assertState { }` | The last state, or a condition on it.                               |
| `assertEvents(...)` / `assertNoEvents()` | The posted events.                                                  |

```kotlin
class LoginViewModelTest {

    @BeforeTest
    fun setUp() {
        useTestDispatchers()
    }

    @AfterTest
    fun tearDown() {
        resetTestDispatchers()
    }

    @Test
    fun `the right credentials log in the user`() = runTest {
        val viewModel = LoginViewModel(LoginUseCase(FakeRepository()), LoginState.DEFAULT)
        val recorder = viewModel.record()

        viewModel.dispatch(LoginAction.UserNameWritten("Admin"))
        viewModel.dispatch(LoginAction.PasswordWritten("1234"))
        viewModel.dispatch(LoginAction.Login)
        advanceUntilIdle()

        recorder.assertState { !it.isLoading }
        recorder.assertEvents(LoginEvent.Message("Carmabs"), LoginEvent.LoginSuccess(admin))
    }
}
```

- **Create the ViewModel after `useTestDispatchers`**, so it runs on the dispatcher of the test. By default it is an
  `UnconfinedTestDispatcher`, which processes the actions as soon as they are dispatched; pass a
  `StandardTestDispatcher` to process them when the test advances.
- **Record before the first action** to record everything.
- **`advanceUntilIdle()`** runs the side effects, delays included, because `runTest` uses the scheduler of the test
  dispatcher.
- **Several ViewModels** can be tested together, for example the results broadcast between screens: the
  `LoginViewModelTest` of the sample creates the profile creation, home and login ViewModels and checks the user
  created in the first one reaches the last one.

## Testing the classic ViewModels

Add `kotlinx-coroutines-test` to your test dependencies:

```kotlin
class CounterViewModelTest {

    private fun TestScope.createViewModel() =
        CounterViewModel(UnconfinedTestDispatcher(testScheduler)).apply {
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

The tests of the library itself, in `emanate-core/src/commonTest`, have more examples: side effects, events,
results between screens and the view lifecycle.

### Things to know

1. **Pass your own dispatcher.** The second constructor parameter of `EmaViewModelClassic` / `EmaViewModelAction` is
   the `CoroutineDispatcher` where the ViewModel runs; it creates its own scope with it. Expose it in the constructor of
   your ViewModels, with a default value, so tests can replace it. The default one is the `main` dispatcher of the
   configuration, `Dispatchers.Main`, which does not exist in unit tests. With an `UnconfinedTestDispatcher` the work runs immediately; with a `StandardTestDispatcher`, call
   `advanceUntilIdle()` to run it.

   ```kotlin
   class CounterViewModel(dispatcher: CoroutineDispatcher = Emanate.configuration.dispatchers.main) :
       EmaViewModelAction<CounterState, CounterAction, CounterEvent>(CounterState(), dispatcher)
   ```
2. **No configuration is needed.** If `Emanate.init` is not called, the default configuration is used. Do not call
   `Emanate.init` in tests: it can be called only once per process.
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
