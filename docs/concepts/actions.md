# Actions

An action is **something the user did**: "tapped login", "wrote a name", "confirmed the dialog".
Actions go from the view to the ViewModel.

```kotlin
sealed interface LoginAction : EmaAction.Screen {

    data object Login : LoginAction
    data object DeleteUser : LoginAction
    data class UserNameWritten(val user: String) : LoginAction
    data class PasswordWritten(val password: String) : LoginAction

    sealed interface Error : LoginAction {
        data object BadCredentialsAccepted : Error
        data object BackPressed : Error
    }
}
```

## Naming

Name actions after **what the user did**, in the past tense, not after what the ViewModel should do.
`UserNameWritten` is better than `UpdateUserName`: the view reports the fact and the ViewModel decides what to do with it.

Use a `sealed interface` so that `when` is exhaustive. When you add an action the compiler tells you everywhere you must handle it.

## Handling actions

Extend `EmaViewModelAction<State, Action, Event>` and implement `onAction`:

```kotlin
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    initialDataState: LoginState
) : EmaViewModelAction<LoginState, LoginAction, LoginEvent>(initialDataState) {

    override fun onAction(action: LoginAction) {
        when (action) {
            LoginAction.Login -> onActionLogin()
            LoginAction.DeleteUser -> onActionDeleteUser()
            is LoginAction.UserNameWritten -> onActionUserWrite(action.user)
            is LoginAction.PasswordWritten -> onActionPasswordWrite(action.password)
            LoginAction.Error.BadCredentialsAccepted -> hideOverlap()
            LoginAction.Error.BackPressed -> hideOverlap()
        }
    }
}
```

`onAction` is `protected`. The public entry point is `dispatch(action)`, so a view can only communicate through
actions and cannot call arbitrary ViewModel functions.

If you prefer not to declare actions, `EmaViewModelBasic<State, Event>` lets the view call public functions of the
ViewModel, as in classic MVVM. Actions are recommended: see [ViewModel](viewmodel.md).

## Dispatching from the view

A Compose screen content receives an `EmaImmutableActionDispatcher`, a stable wrapper that does not cause unnecessary
recompositions:

```kotlin
Button(onClick = { actions.dispatch(LoginAction.Login) }) { ... }
```

For previews use `EmaImmutableActionDispatcherEmpty()`, which ignores every action. Outside Compose,
`EmaActionDispatcherEmpty()` does the same. With Android Views the actions are sent with `viewModel.dispatch(...)`,
see [Android Views](../android-view/screens.md#sending-actions).

## Special action types

| Type                      | Use                                                               |
|---------------------------|-------------------------------------------------------------------|
| `EmaAction.Screen`        | The actions a user can perform on a screen. Your actions extend this. |
| `EmaAction.Initializer`   | The data a screen starts with. See [Initializers](../guides/initializers.md). |
