# Diálogos en Compose

Los diálogos siguen la misma regla que todo lo demás en pantalla: **forman parte del estado**.
El ViewModel asigna un campo, y la pantalla pinta el diálogo mientras el campo lo indique.

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

Como es estado, el diálogo se restaura después de una rotación y el ViewModel sabe si está abierto.

## Pintarlo

No hay nada especial que aprender: emite un composable de diálogo mientras el estado lo indique, e informa de lo que
elija el usuario **mediante acciones**:

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

El sample construye sus diálogos sobre el `AlertDialog` de Material 3, con una data class que implementa `EmaDialogData`
y un listener que hereda de `EmaDialogListener`. Un contenido de pantalla base, `BaseScreenComposable`, da a todas las
pantallas las funciones `ShowDialog`, `ShowError` y `ShowLoading`.

Mientras haya un diálogo en pantalla, devuelve `null` en `onBack`: el diálogo gestiona el botón atrás por sí mismo.
Consulta [Botón atrás](back-handling.es.md).

## Carga

Un indicador de carga no tiene por qué ser un diálogo. Modélalo como un booleano del estado y pinta un indicador de
progreso donde corresponda, por ejemplo dentro del botón que inició el trabajo:

```kotlin
updateState { copy(isLoading = true) }
```

Usa un diálogo de carga bloqueante solo cuando el usuario no deba interactuar con la pantalla.
