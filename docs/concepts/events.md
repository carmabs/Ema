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

The view handles each event once, in the order they were posted. A Compose screen handles them in two places:
the `onEvent` lambda of the navigation graph, for navigation, and `onEvent` of the screen content, for the rest.

```kotlin
// Navigation graph: the events that navigate
createComposableScreen(
    screenContent = LoginScreenContent(),
    viewModel = { LoginViewModel(loginUseCase, LoginState.DEFAULT) },
    onEvent = { event -> if (event is LoginEvent.LoginSuccess) navigator.goToHome(event.user) }
)

// Screen content: the rest
override suspend fun onEvent(
    context: Context,
    event: LoginEvent,
    actions: EmaImmutableActionDispatcher<LoginAction>
) {
    when (event) {
        is LoginEvent.Message -> showMessage(context.getString(R.string.login_welcome, event.userName))
        is LoginEvent.LastUserAdded -> showMessage(context.getString(R.string.login_last_user_added, event.user.fullName))
        is LoginEvent.LoginSuccess -> Unit
    }
}
```

See [Compose screens](../compose/screens.md#two-places-to-handle-events). With Android Views, events are handled in
`onEvent` of the Fragment or Activity, see [Android Views](../android-view/screens.md).

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

- **Only while the screen is visible.** Compose screens receive events while the lifecycle is `STARTED`;
  Fragments and Activities from `onResume` until `onStop`.
- **An equal event is delivered again after the previous one was consumed.** Posting `Message("Ana")` twice, one after
  the other has been handled, shows the message twice.
- **Not persisted.** Events live in the ViewModel, so they survive configuration changes but not process death.

> **Keep `onEvent` short.** If a new event arrives while a previous `onEvent` is suspended,
> the delivery restarts and the unfinished event can be delivered again. Do quick work in `onEvent` and
> send long-running work back to the ViewModel as an action.

## Events or state

If it must be there when the user comes back, it is [state](state.md), not an event.
See the table in [Architecture](architecture.md#state-or-event).

## Screens with no events

Use `EmaEvent.EMPTY` as the event type, and `EmaViewModel.EMPTY` for a screen with no state, actions or events,
like an activity that only hosts other screens.
