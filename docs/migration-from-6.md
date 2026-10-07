# Migrating from 6.x

Version 7 simplifies the model. The sealed `EmaState<S, N>` wrapper (with its *normal*, *overlapped* and *alternative* variants)
and the separate navigation and single-event channels are gone. There are now only:

- **State**: your own data class, the only thing the view renders.
- **Events**: one channel for everything that happens once (navigation, messages...).

This is a breaking change. Most of the migration is mechanical.

## Quick reference

### Types

| 6.x                                   | 7.0                                                   |
|---------------------------------------|-------------------------------------------------------|
| `EmaDataState`                        | `EmaState`                                            |
| `EmaState<S, N>` (`Normal`, `Overlapped`, `Alternative`) | Removed. Use your data class                |
| `EmaNavigationEvent`                  | `EmaEvent`                                            |
| `EmaDataState.EMPTY`, `EmaNavigationEvent.EMPTY` | `EmaState.EMPTY`, `EmaEvent.EMPTY`         |
| `EmaViewModel<S, N>`                  | `EmaViewModel<S, E>`                                  |
| `EmaView<S, VM, N>`                   | `EmaView<S, VM, E>`                                   |
| `EmaComposableScreenContent<S, A>`    | `EmaComposableScreenContent<S, A, E>`                 |
| `SaveStateManager`, `emaSaveStateManager { }` | `EmaSaveStateManager` (a `fun interface`)     |

### In the ViewModel

| 6.x                                              | 7.0                                                  |
|--------------------------------------------------|------------------------------------------------------|
| `updateToNormalState { copy(...) }`, `updateState { }`, `modifyDataState { }` | `updateState { copy(...) }` |
| `updateToNormalState()`                          | `updateState { copy(overlap = null) }`, i.e. clear the field |
| `updateToOverlappedState(EmaExtraData(...))`     | A nullable field in your state (`overlap`, `isLoading`...) |
| `navigate(navigationEvent)`                      | `postEvent(event)`                                   |
| `notifySingleEvent(EmaExtraData(...))`           | `postEvent(event)`                                   |
| `stateData`, `getDataState()`                    | `state`                                              |
| `setBackBroadcastData(data)`                     | `dispatchBroadcast(data)`                            |
| `subscribeStateUpdates()`                        | `stateFlow`                                          |
| `subscribeToNavigationEvents()`, `subscribeToSingleEvents()` | `eventFlow`                              |
| `consumeSingleEvent()`, `notifyOnNavigated()`    | `consumeEvent(event)`, called by Ema                 |
| `onActionBackHardwarePressed()`                  | Removed. See [Back handling](guides/back-handling.md) |

### In views (XML)

| 6.x                                                | 7.0                                                |
|----------------------------------------------------|----------------------------------------------------|
| `onStateNormal(data)`                              | `onState(state)`                                   |
| `onStateOverlapped(extra)`, `onSingleEvent(extra)` | State fields and `onEvent(event)`                  |
| `onNavigation(event)`                              | `onEvent(event)` and `navigate(event)`             |
| `EmaNavigator.navigate(navigationEvent)`           | `navigate(event)`                                  |
| `EmaComposableFragment.onStateNormal(...)`         | `onRenderState(state)`                             |

### In Compose

| 6.x                                                         | 7.0                                                |
|-------------------------------------------------------------|----------------------------------------------------|
| `onStateNormal`, `onStateOverlapped`                        | `onState(state, actions)`                          |
| `createComposableScreen(onNavigationEvent = { })`           | `createComposableScreen(onEvent = { })`            |
| `createComposableScreen(onBackEvent = { data, actions -> })`| `onBack(state)` in the screen content              |
| `emaViewModelSharedDelegate` and extra view models           | Removed. See below                                 |

## Step by step

### 1. Rename the types

`EmaDataState` → `EmaState`, `EmaNavigationEvent` → `EmaEvent`. Add the event type parameter to your views and
screen contents.

### 2. Turn the overlapped state into fields

In 6.x a screen was either in its normal state or covered by an overlapped one (loading, a dialog, an error), which carried an
`EmaExtraData`. Now these are fields of the same state.

```kotlin
// 6.x
showError(LoginOverlap.ErrorBadCredentials)                 // updateToOverlappedState(...)
updateToNormalState()                                       // hide

// 7.0
updateState { copy(overlap = LoginOverlap.ErrorBadCredentials) }
updateState { copy(overlap = null) }
```

In the view, replace `onStateOverlapped` with a `bindForUpdate(state::overlap)` block (or an `if` in Compose).
See [Dialogs](guides/dialogs.md).

### 3. Replace single events and navigation with events

```kotlin
// 6.x
notifySingleEvent(EmaExtraData(data = LoginSingleEvent.Message(user.name)))
navigate(LoginNavigationEvent.LoginSuccess(user))

// 7.0
postEvent(LoginEvent.Message(user.name))
postEvent(LoginEvent.LoginSuccess(user))
```

Merge your `*SingleEvent` and `*NavigationEvent` types into one `*Event`. Rename them to say **what happened**,
see [Events](concepts/events.md#naming). Handle them in `onEvent`, and call `navigate(event)` for the ones that
move the user.

### 4. Update the navigators

The navigator method is now `navigate(event: E)`. Events that do not navigate are ignored with `Unit`.

### 5. Replace hardware back handling

- **Views**: the default (`navigateBack()`) needs no code. To intercept, see [Back handling](guides/back-handling.md).
- **Compose**: replace `onBackEvent` with `onBack(state)` in the screen content, returning the action to send.

### 6. Update view model sharing

`emaViewModelSharedDelegate` and `addExtraViewModel` were removed. To share a ViewModel between fragments of the same
activity, override `fragmentViewModelScope = false`. If a screen needs to react to a second ViewModel, collect its `stateFlow` yourself.

### 7. Write text fields carefully

If you used `modifyDataState` to store text without notifying the view, now every `updateState` notifies it.
Write the text into the field only when it differs from what it already shows, see
[Text fields](guides/xml-views.md#text-fields).

## Artifacts

| 6.x                                          | 7.0                                                |
|----------------------------------------------|----------------------------------------------------|
| `com.github.carmabs.ema:ema-android:6.x`     | `com.github.carmabs.ema:ema-android:7.0.0`         |
| `com.github.carmabs.ema:ema-compose:6.x`     | `com.github.carmabs.ema:ema-compose:7.0.0`         |
| `com.github.carmabs.ema:ema-core:6.x`        | `com.github.carmabs.ema:ema-core:7.0.0`            |
