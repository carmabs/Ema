# Compose dialogs

Dialogs follow the same rule as everything else on screen: **they are part of the state**.
The ViewModel sets a field, the screen draws the dialog while the field says so.

```kotlin
sealed interface ProfileCreationOverlap {
    data object DialogBackConfirmation : ProfileCreationOverlap
}

data class ProfileCreationState(/* ... */ val overlap: ProfileCreationOverlap?) : EmaState
```

```kotlin
// ViewModel
private fun showOverlap(overlap: ProfileCreationOverlap) = updateState { copy(overlap = overlap) }
private fun hideOverlap() = updateState { copy(overlap = null) }
```

Because it is state, the dialog is restored after a rotation and the ViewModel knows whether it is open.

## Drawing it

There is nothing special to learn: emit a dialog composable while the state says so, and report the user choices
**as actions**:

```kotlin
state.overlap?.also { overlap ->
    when (overlap) {
        ProfileCreationOverlap.DialogBackConfirmation -> SimpleDialogComposable(
            dialogData = SimpleDialogData(
                title = EmaText.id(R.string.profile_creation_user_exit_title),
                message = EmaText.id(R.string.profile_creation_user_exit_message),
                showCancel = true
            ),
            dialogListener = object : SimpleDialogListener {
                override fun onCancelClicked() = actions.dispatch(ProfileCreationAction.DialogBackCancel)
                override fun onConfirmClicked() = actions.dispatch(ProfileCreationAction.DialogBackConfirm)
                override fun onBackPressed() = actions.dispatch(ProfileCreationAction.DialogBackCancel)
            }
        )
    }
}
```

The sample builds its dialogs on Material 3's `AlertDialog`, with a data class that implements `EmaDialogData` and a
listener that extends `EmaDialogListener`. A base screen content, `BaseScreenComposable`, gives every screen the
`ShowDialog`, `ShowError` and `ShowLoading` functions.

While a dialog is on screen, return `null` from `onBack`: the dialog handles the back button itself.
See [Back handling](back-handling.md).

## Loading

A spinner is not necessarily a dialog. Model it as a boolean in the state and draw a progress indicator where it
belongs, for example inside the button that started the work:

```kotlin
updateState { copy(isLoading = true) }
```

Use a blocking loading dialog only when the user must not interact with the screen.
