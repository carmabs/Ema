# Estado

El estado es una `data class` inmutable con **todo lo que la pantalla necesita para pintarse**.

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

## Reglas

- **Hazlo una `data class`.** Con `copy` se crea el siguiente estado, y su `equals` permite a la vista ignorar los
  estados que no han cambiado. Usa `EmaState.EMPTY` en pantallas sin estado.
- **Hazlo inmutable.** Usa `val` y colecciones de solo lectura. Cámbialo con `copy`.
- **Dale un `DEFAULT`.** Será el estado inicial y es útil para las previews y los tests.
- **Añade propiedades derivadas en lugar de duplicar datos.** Mantienen la vista libre de lógica:

```kotlin
data class HomeState(
    val userData: UserData?,
    val userList: List<User>
) : EmaState {

    val showCreateButton get() = userData?.role == Role.ADMIN
    val showEmptyList get() = userData != null && userList.isEmpty()
}
```

## Cambiar el estado

Dentro de un ViewModel usa `updateState`, que recibe el estado actual como `this`:

```kotlin
updateState { copy(isLoading = true) }
```

Lee el valor actual con la propiedad protegida `state`:

```kotlin
if (state.isLoading) return
```

El estado se expone como `stateFlow: StateFlow<S>`. Al ser un `StateFlow`, dos estados iguales seguidos no se entregan
dos veces y los colectores lentos solo ven el último valor.

## Modelar diálogos, carga y errores

Ema no tiene estados especiales de "carga" o "error". Son campos normales de tu estado, así que sobreviven a una rotación
y son fáciles de testear.

**Un diálogo**: un tipo sellado nullable. `null` significa que no hay diálogo.

```kotlin
sealed interface LoginOverlap {
    data object ErrorBadCredentials : LoginOverlap
}

// ViewModel
updateState { copy(overlap = LoginOverlap.ErrorBadCredentials) }  // mostrar
updateState { copy(overlap = null) }                              // ocultar
```

**Carga**: un booleano.

```kotlin
updateState { copy(isLoading = true) }
```

**Errores de los campos**: un booleano (o un mensaje) por campo. Bórralo cuando el usuario edita el campo:

```kotlin
private fun onActionUserWrite(user: String) {
    updateState { copy(userName = user, userNameError = false) }
}
```

## Pintar el estado

En Compose simplemente pintas el estado: la recomposición calcula las diferencias. Consulta
[Pantallas de Compose](../compose/screens.es.md). Con las vistas de Android, `bindForUpdate` ejecuta cada parte del código
de UI solo cuando su campo ha cambiado: consulta [Vistas de Android](../android-view/screens.es.md#renderizar-con-bindforupdate).

## Lo que *no* va en el estado

- Lo que ocurre una sola vez: snackbars, toasts, navegación. Usa [eventos](events.es.md).
- Tipos de Android (`Context`, `View`, `Bitmap`...). Usa `EmaText` para los textos con recursos, consulta
  [Utilidades](../guides/utilities.es.md).
- Colecciones mutables u objetos que cambian sin que llames a `copy`.
