# ViewModel

An Emanate ViewModel is a plain Kotlin class with no Android dependencies. There are two ways to write it:

| Class                         | Style                                                                                   |
|-------------------------------|-----------------------------------------------------------------------------------------|
| `EmaViewModel<S, A, E>`       | Declarative: reducers, middlewares and side effects for each type of action. **Recommended.** |
| `EmaViewModelAction<S, A, E>` | Classic, imperative: each action is handled in `onAction`, which updates the state directly. |

`EmaViewModel` is the recommended choice. Its state is held by an [`EmaSource`](source.md): every action goes
through the middlewares, the reducers and the side effects of its type, and the state only changes in the reducers,
which are pure functions. The flow of every screen is the same and predictable, the business logic is split in small
pieces declared in one place, and the whole ViewModel can be tested by recording its actions, states and events with
[emanate-test](../guides/testing.md).

The classic ViewModels, `EmaViewModelAction` and `EmaViewModelClassic`, are kept for compatibility and to migrate the
existing screens step by step. Both kinds can live in the same app: the views only depend on `EmaViewModelContract`.

## EmaViewModel

Extend `EmaViewModel<State, Action, Event>`, pass the initial state and declare in `setup` how the actions are
handled. `source { }` creates the setup with the types of the ViewModel, so they aren't written again:

```kotlin
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    initialDataState: LoginState
) : EmaViewModel<LoginState, LoginAction, LoginEvent>(initialDataState) {

    override fun setup() = source {
        reducers {
            add<LoginAction> { action ->
                when (action) {
                    is LoginAction.UserNameWritten -> state.copy(userName = action.user, userNameError = false)
                    LoginAction.Login -> state.copy(userNameError = state.userName.isBlank())
                    // ...
                }
            }
        }
        sideEffects {
            add<LoginAction.Login>(concurrency = SideEffectConcurrency.Leading) {
                if (state.userNameError) return@add
                dispatch(LoginRequest.Started)
                loginUseCase(LoginUseCase.Input(state.userName, state.userPassword))
                    .onSuccess { user -> dispatch(LoginRequest.Succeeded(user)) }
                    .onFailure { dispatch(LoginRequest.Failed) }
            }
            add<LoginRequest.Succeeded> { action -> postEvent(LoginEvent.LoginSuccess(action.user)) }
        }
    }
}
```

| In `source { }`                    | Purpose                                                                    |
|------------------------------------|----------------------------------------------------------------------------|
| `reducers { add<Type> { } }`       | Returns the next state for the actions of that type. Pure: it only reads `state`. |
| `middlewares { add<Type> { } }`    | Decides the path of the actions of that type before the reducers: `next()`, `nextAfter { }`... |
| `sideEffects { add<Type> { } }`    | Business logic run after the reducers: use cases, `dispatch`, `postEvent`. |
| `broadcasts { listen<Type>() }`    | Receives the actions of that type broadcast by other ViewModels. See [Results between screens](../guides/results-between-screens.md). |

Inside each block, `+` adds a piece already created, like `+middlewareDelayOf(...)` or a reducer declared as a
property.

Everything about reducers, middlewares and side effects is in [Source](source.md).

The constructor receives, besides the initial state:

- `dispatcher`: where the ViewModel runs. By default the `main` dispatcher of the [configuration](../guides/configuration.md).
- `logger`: the `EmaLogger` that logs every action with the state before and after it, with the name of the
  ViewModel as tag. By default the `logger` of the [configuration](../guides/configuration.md#logs),
  `EmaLogger.Println` unless it is changed; pass `null` to disable the logs of this ViewModel.

`setup` is called the first time the ViewModel is used, not in the constructor, so it can use the properties of your
class, like its use cases.

### Internal actions

The side effects report their results with actions, so the reducers can apply them. When an action is only dispatched
by the ViewModel, declare it `internal` in the same module, next to the actions of the screen, so the view can't send it:

```kotlin
internal sealed interface LoginRequest : LoginAction {
    data object Started : LoginRequest
    data class Succeeded(val user: User) : LoginRequest
    data object Failed : LoginRequest
}
```

### The lifecycle as actions

`EmaViewModel` has no lifecycle hooks: the lifecycle of the view arrives as actions, which the middlewares, reducers
and side effects handle like any other.

| Action                            | When                                                                     |
|-----------------------------------|--------------------------------------------------------------------------|
| `EmaAction.Initializer`           | **Once**, the first time the screen is created: its [initializer](../guides/initializers.md), or `Initializer.EMPTY`. |
| `EmaAction.Lifecycle.Started`     | The screen becomes visible.                                              |
| `EmaAction.Lifecycle.Resumed`     | The screen is in the foreground.                                         |
| `EmaAction.Lifecycle.Paused`      | The screen is no longer fully visible.                                   |
| `EmaAction.Lifecycle.Stopped`     | The screen goes to the background.                                       |
| `EmaAction.Lifecycle.Destroyed`   | The ViewModel is cleared. Then its work is cancelled, except its side effects. |

`Destroyed` is the last action. The rest of the work of the ViewModel is cancelled after it, but its side effects can
finish, up to their `timeout` or the `destroyedTimeout` of the
[configuration](../guides/configuration.md), 5 seconds by default, for example to send a result to the previous screen:

```kotlin
sideEffects {
    add<EmaAction.Lifecycle.Destroyed> {
        broadcast(EditorBroadcast.DraftClosed(state.draft))
    }
}
```

They can broadcast actions and read the state. The actions they dispatch and the events they post are ignored, as the
ViewModel is closed.

Loading the initial data is a side effect of the initializer:

```kotlin
sideEffects {
    add<HomeInitializer> { initializer ->
        when (initializer) {
            is HomeInitializer.HomeUser -> {
                val friends = getUserFriendsUseCase(GetUserFriendsUseCase.Input(initializer.user))
                dispatch(HomeFriendsLoaded(friends))
            }
        }
    }
}
```

## Classic ViewModels

`EmaViewModelAction<S, A, E>` receives the actions in `onAction` and changes the state imperatively with
`updateState`. `EmaViewModelClassic<S, E>` doesn't declare actions: the view calls public functions of the ViewModel,
as in classic MVVM. Prefer `EmaViewModelAction` between the two: its single entry point, `dispatch`, and the `sealed`
actions make the code easier to follow.

Both receive the initial state in the constructor and, optionally, the `CoroutineDispatcher` where their work runs
(by default the main dispatcher of the [configuration](../guides/configuration.md)). They create their own scope with
it and cancel it when they are cleared.

```kotlin
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    initialDataState: LoginState
) : EmaViewModelAction<LoginState, LoginAction, LoginEvent>(initialDataState) {

    override fun onStateCreated(initializer: EmaAction.Initializer?) = Unit

    override fun onAction(action: LoginAction) { /* ... */ }
}
```

### What you can use inside

| Member                                        | Purpose                                                                  |
|-----------------------------------------------|--------------------------------------------------------------------------|
| `state`                                       | The current state (read only).                                           |
| `updateState { copy(...) }`                   | Replaces the state and notifies the view.                                |
| `postEvent(event, allowDuplicated)`           | Sends a one-shot event. See [Events](events.md).                         |
| `sideEffect { }` / `singleSideEffect(id) { }` | Runs suspending work. See [Async work and errors](../guides/async-and-errors.md). |
| `broadcast(action, keepUntilReceived)`        | Broadcasts an action, for example a result for the previous screen. See [Results between screens](../guides/results-between-screens.md). |
| `registerBroadcastListener<A> { }`            | Receives the broadcast actions of type `A`.                              |
| `scope`                                       | The `CoroutineScope` of the ViewModel.                                   |

### Lifecycle hooks

The ViewModel follows the lifecycle of its screen, and the hooks let it react to each stage without knowing anything
about Android:

- **Creation**, to prepare the screen: read the initializer, load the initial data and register the listeners of
  results. It happens once, so a rotation does not load the data again.
- **Visibility**, to run work only while the user sees the screen: start location updates, a timer or a periodic
  refresh when it becomes visible, and stop them when it goes to the background.
- **Destruction**, to release what the ViewModel holds when its screen is closed for good.

`onStateCreated` is required; override the others only when you need them.

| Hook                          | When                                                                     |
|-------------------------------|--------------------------------------------------------------------------|
| `onStateCreated(initializer)` | **Once**, the first time the screen is created. Receives the [initializer](../guides/initializers.md). |
| `onBroadcastListenerSetup()`  | Right after, to register [result listeners](../guides/results-between-screens.md). |
| `onViewStarted()`             | The screen becomes visible.                                              |
| `onViewResumed()`             | The screen is in the foreground.                                         |
| `onViewPaused()`              | The screen is no longer fully visible.                                   |
| `onViewStopped()`             | The screen goes to the background.                                       |
| `onDestroy()`                 | The ViewModel is destroyed. The scope is already cancelled.              |

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

## The contract with the view

The views don't depend on the implementation of the ViewModel, but on `EmaViewModelContract<S, E>`: the initial
state, `stateFlow`, `eventFlow`, `consumeEvent` and the lifecycle calls (`onCreated`, `onStartView`, `onResumeView`,
`onPauseView`, `onStopView` and `onCleared`). `EmaViewModel`, `EmaViewModelAction` and `EmaViewModelClassic` implement
it, and `EmaViewModelContract.EMPTY` is the ViewModel of a screen without state, actions nor events.

## How the ViewModel is kept alive

On Android, Emanate keeps your ViewModel inside an Android `ViewModel` (`EmaAndroidViewModel`), so it lives as long as
its screen and survives configuration changes:

1. The first time a screen is opened, Emanate calls the function you gave it (`viewModel` in Compose,
   `provideViewModel()` in Views) and keeps that instance.
2. While the screen exists, after rotations or when coming back from the back stack, Emanate reuses the instance it
   keeps. Your function is not called again.
3. When the screen is closed, the ViewModel is cleared. Opening the screen again creates a new one.

Each screen therefore has its own ViewModel, created once. See [Dependency injection](../guides/dependency-injection.md).

The ViewModel is identified by its class name (`id`). When the Android ViewModel is cleared it calls `onCleared`:
an `EmaViewModel` processes the pending actions and `EmaAction.Lifecycle.Destroyed`, and then cancels its work,
except the side effects of `Destroyed`; a classic ViewModel cancels its `scope`.

## Rendering before the first update

`updateOnInitialization` (protected, `true` by default) controls whether a Compose screen draws the initial state
straight away. Override it with `false` to render only after the state is updated for the first time: in an
`EmaViewModel`, after the first action of the screen or initializer other than `Initializer.EMPTY`.
