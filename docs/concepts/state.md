# State

The state is an immutable `data class` with **everything the screen needs to draw itself**.

```kotlin
data class LoginState(
    val userName: String,
    val userPassword: String,
    val isLoading: Boolean,
    val userNameError: Boolean,
    val passwordError: Boolean,
    val overlap: LoginOverlap?
) : EmaState {

    companion object {
        val DEFAULT = LoginState(
            userName = STRING_EMPTY,
            userPassword = STRING_EMPTY,
            isLoading = false,
            userNameError = false,
            passwordError = false,
            overlap = null
        )
    }
}
```

## Rules

- **It must be a `data class`.** Ema checks it when the ViewModel is created and throws
  `IllegalStateException` otherwise. `EmaState.EMPTY` is the only exception, for screens with no state.
- **Make it immutable.** Use `val` and read-only collections. Change it with `copy`.
- **Give it a `DEFAULT`.** It becomes the initial state and is handy for previews and tests.
- **Add derived properties instead of duplicating data.** They keep the view free of logic:

```kotlin
data class HomeState(
    val userData: UserData?,
    val userList: List<User>
) : EmaState {

    val showCreateButton get() = userData?.role == Role.ADMIN
    val showEmptyList get() = userData != null && userList.isEmpty()
}
```

## Changing the state

Inside a ViewModel use `updateState`, which receives the current state as `this`:

```kotlin
updateState { copy(isLoading = true) }
```

Read the current value with the protected `state` property:

```kotlin
if (state.isLoading) return
```

The state is exposed as `stateFlow: StateFlow<S>`. Because it is a `StateFlow`,
equal consecutive states are not delivered twice and slow collectors only see the latest value.

## Modelling dialogs, loading and errors

Ema does not have special "loading" or "error" states. They are ordinary fields of your state, so they survive
a rotation and are easy to test.

**A dialog**: a nullable sealed type. `null` means no dialog.

```kotlin
sealed interface LoginOverlap {
    data object ErrorBadCredentials : LoginOverlap
}

// ViewModel
updateState { copy(overlap = LoginOverlap.ErrorBadCredentials) }  // show
updateState { copy(overlap = null) }                              // hide
```

**Loading**: a boolean.

```kotlin
updateState { copy(isLoading = true) }
```

**Field errors**: one boolean (or a message) per field. Clear it when the user edits the field:

```kotlin
private fun onActionUserWrite(user: String) {
    updateState { copy(userName = user, userNameError = false) }
}
```

## Rendering the state

On Views, use `bindForUpdate` to run each piece of UI code only when its field changed.
See [Views with XML](../guides/xml-views.md). On Compose you simply draw the state: recomposition does the diffing.

## What does *not* belong in the state

- One-shot things: snackbars, toasts, navigation. Use [events](events.md).
- Android types (`Context`, `View`, `Bitmap`...). Use `EmaText` for strings with resources, see [Utilities](../guides/utilities.md).
- Mutable collections or objects that change without you calling `copy`.
