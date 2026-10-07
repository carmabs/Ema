# Events

An event is **something that happened in the feature** and that the view must handle **once**:
show a message, open another screen, close the screen.

```kotlin
sealed interface LoginEvent : EmaEvent {
    data class Message(val userName: String) : LoginEvent
    data class LastUserAdded(val user: User) : LoginEvent
    data class LoginSuccess(val user: User) : LoginEvent
}
```

## Posting events

From the ViewModel, call `postEvent`:

```kotlin
userLogged.onSuccess { user ->
    postEvent(LoginEvent.Message(user.name))
    postEvent(LoginEvent.LoginSuccess(user))
}
```

## Handling events

Implement `onEvent` in the view. It is called once per event, in the order they were posted.

```kotlin
override suspend fun LoginFragmentBinding.onEvent(event: LoginEvent) {
    when (event) {
        is LoginEvent.LoginSuccess -> navigate(event)
        is LoginEvent.LastUserAdded -> showMessage(getString(R.string.login_last_user_added, event.user.fullName))
        is LoginEvent.Message -> showMessage(getString(R.string.login_welcome, event.userName))
    }
}
```

In Compose, events can be handled in two places, see [Compose](../guides/compose.md).

## Naming

**Events say what happened, never what to do.** The receiver decides the reaction.

| Prefer                 | Avoid                 | Why                                                               |
|------------------------|-----------------------|-------------------------------------------------------------------|
| `LoginSuccess(user)`   | `NavigateToHome(user)`| The ViewModel does not know that "success" means navigating.      |
| `UserTypeSelected(role)`| `GoToProfileCreation`| Which screen follows is the navigator's decision.                 |
| `OnBoardingCancelled`  | `CloseScreen`         | The view could also just hide a pane.                             |
| `LastUserAdded(user)`  | `ShowToast(user)`     | It could be a toast, a snackbar or nothing at all.                |

A good test: the name still makes sense if you replace the screen's UI completely.

## Delivery rules

- **Once.** After `onEvent` returns, the event is consumed. It is not replayed when the view is recreated.
- **Not lost.** Events are queued in the ViewModel until a view is able to receive them. If you post an event while the screen is in the background, or during a rotation, it is delivered afterwards.
- **In order.** Several pending events are delivered as a batch, in the order they were posted.
- **Not duplicated by default.** If an equal event is already waiting, `postEvent` ignores the new one. Pass `allowDuplicated = true` to queue it anyway:

```kotlin
postEvent(LoginEvent.Message("Ana"), allowDuplicated = true)
```

- **Only while the screen is visible.** Views receive events from `onResume` until `onStop`; Compose screens while the lifecycle is `STARTED`.
- **Not persisted.** Events live in the ViewModel, so they survive configuration changes but not process death.

> **Keep `onEvent` short.** If a new event arrives while a previous `onEvent` is suspended,
> the delivery restarts and the unfinished event can be delivered again. Do quick work in `onEvent` and
> send long-running work back to the ViewModel as an action.

## Events or state

If it must be there when the user comes back, it is [state](state.md), not an event.
See the table in [Architecture](architecture.md#state-or-event).

## Screens with no events

Use `EmaEvent.EMPTY` as the event type:

```kotlin
class SplashActivity : EmaToolbarActivity<SplashActivityBinding, EmaState.EMPTY, EmaViewModel.EMPTY, EmaEvent.EMPTY>()
```
