# Dialogs

Dialogs follow the same rule as everything else on screen: **they are part of the state**.
The ViewModel sets a field, the view shows or hides the dialog according to it.

```kotlin
sealed interface LoginOverlap {
    data object ErrorBadCredentials : LoginOverlap
}

data class LoginState(/* ... */ val overlap: LoginOverlap?) : EmaState
```

```kotlin
// ViewModel
updateState { copy(overlap = LoginOverlap.ErrorBadCredentials) }

private fun onActionBadCredentialsAccepted() = updateState { copy(overlap = null) }
```

Because it is state, the dialog is restored after a rotation and the ViewModel knows whether it is open.

## Views (XML)

Ema provides the machinery to show dialogs as `DialogFragment`s without duplicates after rotations. You provide three things.

**1. The dialog data.** A class that implements `EmaDialogData`:

```kotlin
data class ErrorDialogData(
    val title: EmaText = EmaText.empty(),
    val message: EmaText = EmaText.empty(),
    override val proportionWidth: Float? = 0.85f,
    override val proportionHeight: Float? = null,
    override val isModal: Boolean = true
) : EmaDialogData
```

`proportionWidth` and `proportionHeight` are a fraction of the screen (`null` wraps the content). When `isModal`
is `true`, the dialog cannot be cancelled by touching outside or pressing back.

**2. The dialog.** Extend `EmaDialog<Binding, Data>`:

```kotlin
class ErrorDialog : EmaDialog<DialogErrorBinding, ErrorDialogData>() {

    override fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        DialogErrorBinding.inflate(inflater, container, false)

    override fun DialogErrorBinding.setup(data: ErrorDialogData) {
        tvDialogErrorTitle.text = data.title.string(requireContext())
        tvDialogErrorMessage.text = data.message.string(requireContext())
        (dialogListener as? ErrorDialogListener)?.also { listener ->
            tvDialogErrorAccept.setOnClickListener { listener.onConfirmClicked() }
        }
    }

    override fun createInitialState() = ErrorDialogData()
}
```

**3. A provider.** It creates the dialog from its data. `EmaAndroidDialogProvider` handles showing, updating and hiding:

```kotlin
class ErrorDialogProvider(fragmentManager: FragmentManager) : EmaAndroidDialogProvider(fragmentManager) {
    override fun generateDialog(dialogData: EmaDialogData?): EmaDialog<*, *> = ErrorDialog()
}
```

If your app has several kinds of dialogs, combine the providers in one, as the sample does with `AppDialogProvider`.

### Showing it from the view

The listener reports back to the ViewModel **as actions**. Set it **before** calling `show`:

```kotlin
protected fun showError(data: ErrorDialogData, listener: ErrorDialogListener) {
    appDialogProvider.dialogListener = listener   // first
    appDialogProvider.show(data)                  // then show
}
```

```kotlin
bindForUpdate(state::overlap) {
    when (it) {
        null -> hideDialog()
        LoginOverlap.ErrorBadCredentials -> showError(
            ErrorDialogData(EmaText.id(R.string.title), EmaText.id(R.string.message)),
            object : ErrorDialogListener {
                override fun onConfirmClicked() = viewModel.dispatch(LoginAction.Error.BadCredentialsAccepted)
                override fun onBackPressed() = viewModel.dispatch(LoginAction.Error.BackPressed)
            }
        )
    }
}
```

`EmaDialogListener` has `onBackPressed()`, `onOutsidePressed()` (which calls `onBackPressed()` by default) and `onDestroyed()`.
`EmaDialogProvider` offers `show`, `hide`, `isVisible` and `dialogListener`.

## Compose

There is nothing special to learn: emit a dialog composable while the state says so.

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
        // ...
    }
}
```

The sample builds its dialogs on Material 3's `AlertDialog`. It reuses the same `...DialogData` and `...DialogListener`
types as the XML dialogs, so a dialog is described once and drawn by either technology.

## Loading

A spinner is not necessarily a dialog. In the sample the login shows a progress indicator inside the button:

```kotlin
updateState { copy(isLoading = true) }
```

Use a blocking loading dialog only when the user must not interact with the screen.
