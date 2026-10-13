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
| `testConfiguration(dispatcher)` | A configuration that runs on the dispatcher of the test, to pass it to the ViewModels instead of calling `useTestDispatchers()`. See [Without changing the configuration of Emanate](#without-changing-the-configuration-of-emanate). |
| `viewModel.record()`            | Records in order the actions, states and events of a ViewModel or a source.  |
| `assertActions(...)`            | The recorded actions, including the ones blocked by a middleware.            |
| `assertStates(...)`             | Every state returned by the reducers.                                         |
| `assertState(state)` / `assertState { }` | The last state, or a condition on it.                               |
| `assertEvents(...)` / `assertNoEvents()` | The posted events.                                                  |
| `viewModel.assertSetup()`       | Fails if a middleware, reducer or side effect never runs, like the ones of a broadcast action that isn't listened to. See [Checking the setup](#checking-the-setup). |

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

### Without changing the configuration of Emanate

`useTestDispatchers()` changes the configuration of Emanate and `Dispatchers.Main`, which are shared by the whole
process, so the tests that use it can't run in parallel in the same JVM. If your ViewModels receive the configuration in
the constructor (see [Explicit configuration](configuration.md#explicit-configuration)), pass them
`testConfiguration(dispatcher)` instead: it doesn't change anything shared, so there is nothing to reset.

```kotlin
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    initialState: LoginState,
    configuration: EmaConfiguration = Emanate.configuration
) : EmaViewModel<LoginState, LoginAction, LoginEvent>(initialState, configuration)

class LoginViewModelTest {

    @Test
    fun `the right credentials log in the user`() = runTest {
        val viewModel = LoginViewModel(
            LoginUseCase(FakeRepository()),
            LoginState.DEFAULT,
            testConfiguration(UnconfinedTestDispatcher(testScheduler))
        )
        val recorder = viewModel.record()
        // ...
    }
}
```

- **Create the dispatcher with `testScheduler`**, the scheduler of `runTest`, so `advanceUntilIdle()` runs the work of
  the ViewModel. `Dispatchers.Main` isn't replaced, so `runTest` doesn't take it from there.
- The rest of the configuration is the default `EmaConfiguration()`, not the one of Emanate. Pass another one as the
  second parameter to change it: `testConfiguration(dispatcher, EmaConfiguration(logger = null))`.
- The [use cases](utilities.md#use-cases) run on the `io` dispatcher of the configuration of Emanate unless they receive
  another one in the constructor: fake them, or pass them the dispatcher of the test.
- **Each `testConfiguration` has its own broadcaster**, so the actions broadcast in a test don't reach the others. The
  ViewModels of a test that send results to each other must receive the same configuration: create it once.

### Checking the setup

A reducer, middleware or side effect for an action that the ViewModel never receives doesn't fail: it never runs. It
happens when a broadcast action is handled but `listen<Type>()` is missing in the `broadcasts` block. `assertSetup()`
checks that every piece can receive actions of its type: actions of the screen, initializers, lifecycle actions or
broadcast actions it listens to.

```kotlin
@Test
fun `the setup is complete`() {
    HomeViewModel(GetUserFriendsUseCase(FakeRepository()), HomeState.DEFAULT).assertSetup()
}
```

It fails with every piece that never runs:

```
The reducer of UserCreated never runs: HomeViewModel doesn't receive UserCreated. If it is broadcast by another
source, add listen<UserCreated>() in the broadcasts block.
```

Only the types that the ViewModel can't receive for sure are reported. An interface can be implemented by an action of
the screen, so a piece for an interface that isn't related to the actions of the screen is not reported.

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

1. **Pass your own dispatcher.** The `dispatcher` parameter of `EmaViewModelClassic` / `EmaViewModelAction` is the
   `CoroutineDispatcher` where the ViewModel runs; it creates its own scope with it. Expose it in the constructor of
   your ViewModels, with a default value, so tests can replace it. The default one is the `main` dispatcher of the
   configuration, `Dispatchers.Main`, which does not exist in unit tests. With an `UnconfinedTestDispatcher` the work
   runs immediately; with a `StandardTestDispatcher`, call `advanceUntilIdle()` to run it.

   ```kotlin
   class CounterViewModel(dispatcher: CoroutineDispatcher = Emanate.configuration.dispatchers.main) :
       EmaViewModelAction<CounterState, CounterAction, CounterEvent>(CounterState(), dispatcher = dispatcher)
   ```

   The second parameter is the [configuration](configuration.md#explicit-configuration), so pass the dispatcher by
   name, or expose the whole configuration instead.
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
