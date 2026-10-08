# ViewModel

An Ema ViewModel is a plain Kotlin class with no Android dependencies. There are two flavours:

| Class                                  | Use it when                                                              |
|----------------------------------------|--------------------------------------------------------------------------|
| `EmaViewModelAction<S, A, E>`          | The view reports what the user did with actions. **Recommended.**        |
| `EmaViewModelBasic<S, E>`              | You prefer not to declare actions: the view calls public functions of the ViewModel, as in classic MVVM. |

`EmaViewModelAction` is the recommended choice. Every input of the screen is an action of a `sealed` type, so the
compiler checks that all of them are handled, the ViewModel has a single entry point (`dispatch`), and the actions
describe what the user did, which makes the code easier to follow and to debug: logging or setting a breakpoint in
`onAction` shows every input. With `EmaViewModelBasic` the view can call any public function, and that structure
depends only on discipline.

Both receive the initial state in the constructor and, optionally, the `CoroutineScope` where their work runs
(by default one on the main dispatcher of the [configuration](../guides/configuration.md)).

```kotlin
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    initialDataState: LoginState
) : EmaViewModelAction<LoginState, LoginAction, LoginEvent>(initialDataState) {

    override fun onStateCreated(initializer: EmaAction.Initializer?) = Unit

    override fun onAction(action: LoginAction) { /* ... */ }
}
```

## What you can use inside

| Member                                  | Purpose                                                                  |
|-----------------------------------------|--------------------------------------------------------------------------|
| `state`                                 | The current state (read only).                                           |
| `updateState { copy(...) }`             | Replaces the state and notifies the view.                                |
| `postEvent(event, allowDuplicated)`     | Sends a one-shot event. See [Events](events.md).                         |
| `sideEffect { }` / `singleSideEffect(id) { }` | Runs suspending work. See [Async work and errors](../guides/async-and-errors.md). |
| `dispatchBroadcast(data)`               | Sends a result to the previous screen. See [Results between screens](../guides/results-between-screens.md). |
| `registerBackBroadcastListener(...)`    | Receives a result from another screen.                                   |
| `scope`                                 | The `CoroutineScope` of the ViewModel.                                   |

## Lifecycle hooks

The lifecycle methods of the base classes are `final`. Override the protected hooks instead:

| The view calls    | Hook you override         | When                                                                    |
|-------------------|---------------------------|-------------------------------------------------------------------------|
| `onCreated`       | `onStateCreated(initializer)` | **Once**, the first time the screen is created. Receives the [initializer](../guides/initializers.md). |
| `onCreated`       | `onBroadcastListenerSetup()`  | Right after, to register [result listeners](../guides/results-between-screens.md). |
| `onStartView`     | `onViewStarted()`         | The screen becomes visible.                                             |
| `onResumeView`    | `onViewResumed()`         | The screen is in the foreground.                                        |
| `onPauseView`     | `onViewPaused()`          | The screen is no longer fully visible.                                  |
| `onStopView`      | `onViewStopped()`         | The screen goes to the background.                                      |
| `onCleared`       | `onDestroy()`             | The ViewModel is destroyed. The scope is already cancelled.             |

`onStateCreated` is the right place to start loading data. It runs once, not on every rotation:

```kotlin
override fun onStateCreated(initializer: EmaAction.Initializer?) {
    val homeUser = (initializer as HomeInitializer.HomeUser).user
    sideEffect {
        val friends = getUserFriendsUseCase(GetUserFriendsUseCase.Input(homeUser))
        updateState { copy(userList = friends) }
    }
}
```

## How the ViewModel is kept alive

On Android, Ema keeps your ViewModel inside an Android `ViewModel` (`EmaAndroidViewModel`), so it lives as long as its
screen and survives configuration changes:

1. The first time a screen is opened, Ema calls the function you gave it (`viewModel` in Compose, `provideViewModel()`
   in Views) and keeps that instance.
2. While the screen exists, after rotations or when coming back from the back stack, Ema reuses the instance it keeps.
   Your function is not called again.
3. When the screen is closed, the ViewModel is cleared. Opening the screen again creates a new one.

Each screen therefore has its own ViewModel, created once. See [Dependency injection](../guides/dependency-injection.md).

The ViewModel is identified by its class name (`id`). Its `scope` is replaced by the `viewModelScope` of the
Android ViewModel, so work is cancelled automatically when the ViewModel is cleared.

## Rendering before the first update

`updateOnInitialization` (protected, `true` by default) controls whether a Compose screen draws the initial state
straight away. Override it with `false` to render only after the first call to `updateState`.

## Pretty printing

`toStringPretty()` prints any object, for example a state, with the `EmaDataClassPrinter` of the
[configuration](../guides/configuration.md#printing-objects). With `EmaConfiguration.Android` it prints every field,
nested objects and collections, indented:

```kotlin
Log.d("Login", state.toStringPretty())
```
