# Botón atrás en Compose

Implementa `onBack` en el contenido de la pantalla. Devuelve la **acción** que se envía al pulsar atrás, o `null` para
que lo gestione el sistema.

```kotlin
override fun onBack(state: ProfileCreationState): ProfileCreationAction? =
    if (state.overlap == null) ProfileCreationAction.OnBack else null
```

Ema instala un único `BackHandler`, activo solo cuando `onBack` devuelve una acción. Devolver `null` en algunos estados es
la forma de desactivar la interceptación.

- Un diálogo pintado con `AlertDialog` o `Dialog` gestiona el botón atrás por sí mismo, así que devuelve `null` mientras
  está en pantalla.
- Recibe el **estado**, así que la decisión puede depender de él: intercepta el botón atrás solo cuando hay cambios sin
  guardar.

## Un ejemplo completo

La pantalla de creación pide confirmación antes de salir. Tanto la flecha de la toolbar como el botón atrás del sistema
envían la misma acción:

```kotlin
// Contenido de la pantalla
IconButton(onClick = { actions.dispatch(ProfileCreationAction.OnBack) }) { /* flecha */ }

override fun onBack(state: ProfileCreationState): ProfileCreationAction? =
    if (state.overlap == null) ProfileCreationAction.OnBack else null
```

```kotlin
// ViewModel
private fun onActionBack() = showOverlap(ProfileCreationOverlap.DialogBackConfirmation)
private fun onActionBackConfirmed() {
    hideOverlap()
    postEvent(ProfileCreationEvent.DialogConfirmationAccepted)  // el navigator cierra la pantalla
}
```

En una pantalla donde la flecha solo vuelve atrás, envía también una acción, para que haya un único camino a través del
ViewModel:

```kotlin
override fun onBack(state: ProfileOnBoardingState) = ProfileOnBoardingActions.BackClicked

// ViewModel
private fun onActionBackClicked() = postEvent(ProfileOnBoardingEvent.OnBoardingCancelled)
```

El navigator convierte el evento en `navigateBack()`. Consulta [Navegación](navigation.es.md).
