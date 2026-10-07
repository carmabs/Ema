# ViewModel

An Ema ViewModel is a plain Kotlin class with no Android dependencies. There are two flavours:

| Class                                  | Use it when                                         |
|----------------------------------------|-----------------------------------------------------|
| `EmaViewModelAction<S, A, E>`          | The screen receives user actions. **The usual choice.** |
| `EmaViewModelBasic<S, E>`              | The screen only shows state and emits events.       |

Both receive the initial state in the constructor. `EmaViewModelAction` adds `dispatch(action)` and the abstract `onAction`.

```kotlin
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    initialDataState: LoginState
) : EmaViewModelAction<LoginState, LoginAction, LoginEvent>(initialDataState) {

    override fun onStateCreated(initializer: EmaInitializer?) = Unit

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
override fun onStateCreated(initializer: EmaInitializer?) {
    sideEffect {
        val friends = getUserFriendsUseCase(GetUserFriendsUseCase.Input(user))
        updateState { copy(userList = friends) }
    }
}
```

## How the ViewModel is kept alive

Ema wraps your ViewModel in an Android `ViewModel` (`EmaAndroidViewModel`) so it survives configuration
changes. The instance created by Koin is only used the first time; later calls return the retained one.
This is why ViewModels are declared as `factory` in Koin: see [Dependency injection](../guides/dependency-injection.md).

The ViewModel is identified by its class name (`id`). The `scope` is replaced by the `viewModelScope` of the
Android ViewModel, so work is cancelled automatically when the ViewModel is cleared.

### Sharing a ViewModel between fragments

By default a Fragment's ViewModel belongs to the Fragment. Override `fragmentViewModelScope` to attach it to the Activity:

```kotlin
override val fragmentViewModelScope = false
```

Fragments of the same Activity that use the same ViewModel class then get the same instance.

## Rendering before the first update

`updateOnInitialization` (protected, `true` by default) controls whether a Compose screen draws the initial state
straight away. Override it with `false` to render only after the first call to `updateState`. It is only honoured by Compose screens.
