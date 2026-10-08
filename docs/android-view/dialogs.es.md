# Diálogos

Los diálogos siguen la misma regla que todo lo demás en pantalla: **forman parte del estado**.
El ViewModel asigna un campo, y la vista muestra u oculta el diálogo según su valor.

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

Como es estado, el diálogo se restaura después de una rotación y el ViewModel sabe si está abierto.

## Crear un diálogo

Ema da la maquinaria para mostrar diálogos como `DialogFragment`s sin duplicados después de una rotación. Tú aportas tres
cosas.

**1. Los datos del diálogo.** Una clase que implementa `EmaDialogData`:

```kotlin
data class ErrorDialogData(
    val title: EmaText = EmaText.empty(),
    val message: EmaText = EmaText.empty(),
    override val proportionWidth: Float? = 0.85f,
    override val proportionHeight: Float? = null,
    override val isModal: Boolean = true
) : EmaDialogData
```

`proportionWidth` y `proportionHeight` son una fracción de la pantalla (`null` se ajusta al contenido). Cuando `isModal`
es `true`, el diálogo no se puede cancelar tocando fuera ni pulsando atrás.

**2. El diálogo.** Hereda de `EmaDialog<Binding, Data>`:

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

**3. Un proveedor.** Crea el diálogo a partir de sus datos. `EmaAndroidDialogProvider` se encarga de mostrarlo,
actualizarlo y ocultarlo:

```kotlin
class ErrorDialogProvider(fragmentManager: FragmentManager) : EmaAndroidDialogProvider(fragmentManager) {
    override fun generateDialog(dialogData: EmaDialogData?): EmaDialog<*, *> = ErrorDialog()
}
```

Si tu app tiene varios tipos de diálogos, combina los proveedores en uno, como hace el sample con `AppDialogProvider`.

## Mostrarlo desde la vista

El listener informa al ViewModel **mediante acciones**. Asígnalo **antes** de llamar a `show`:

```kotlin
protected fun showError(data: ErrorDialogData, listener: ErrorDialogListener) {
    appDialogProvider.dialogListener = listener   // primero
    appDialogProvider.show(data)                  // después, mostrar
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

`EmaDialogListener` tiene `onBackPressed()`, `onOutsidePressed()` (que por defecto llama a `onBackPressed()`) y
`onDestroyed()`. `EmaDialogProvider` ofrece `show`, `hide`, `isVisible` y `dialogListener`.

`hide()` elimina el diálogo por completo, así que no se restaura duplicado después de una rotación.

## Carga

Un indicador de carga no tiene por qué ser un diálogo. En el sample, el login muestra un indicador de progreso dentro
del botón:

```kotlin
updateState { copy(isLoading = true) }
```

Usa un diálogo de carga bloqueante solo cuando el usuario no deba interactuar con la pantalla.
