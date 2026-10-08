# Eventos

Un evento es **algo que ha pasado en la funcionalidad** y que la vista tiene que gestionar **una vez**:
mostrar un mensaje, abrir otra pantalla, cerrar la pantalla.

```kotlin
sealed interface LoginEvent : EmaEvent {
    data class Message(val userName: String) : LoginEvent
    data class LastUserAdded(val user: User) : LoginEvent
    data class LoginSuccess(val user: User) : LoginEvent
}
```

## Publicar eventos

Desde el ViewModel, llama a `postEvent`:

```kotlin
userLogged.onSuccess { user ->
    postEvent(LoginEvent.Message(user.name))
    postEvent(LoginEvent.LoginSuccess(user))
}
```

## Gestionar los eventos

La vista gestiona cada evento una vez, en el orden en que se publicaron. Una pantalla Compose los gestiona en dos sitios:
la lambda `onEvent` del grafo de navegación, para la navegación, y `onEvent` del contenido de la pantalla, para el resto.

```kotlin
// Grafo de navegación: los eventos que navegan
createComposableScreen(
    screenContent = LoginScreenContent(),
    viewModel = { LoginViewModel(loginUseCase, LoginState.DEFAULT) },
    onEvent = { event -> if (event is LoginEvent.LoginSuccess) navigator.goToHome(event.user) }
)

// Contenido de la pantalla: el resto
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

Consulta [Pantallas de Compose](../compose/screens.es.md#dos-sitios-para-gestionar-los-eventos). Con las vistas de
Android, los eventos se gestionan en `onEvent` del Fragment o la Activity, consulta [Vistas de Android](../android-view/screens.es.md).

## Nombres

**Los eventos dicen lo que ha pasado, nunca lo que hay que hacer.** Quien los recibe decide la reacción.

| Mejor                   | Evita                  | Por qué                                                           |
|-------------------------|------------------------|-------------------------------------------------------------------|
| `LoginSuccess(user)`    | `NavigateToHome(user)` | El ViewModel no sabe que "éxito" significa navegar.               |
| `UserTypeSelected(role)`| `GoToProfileCreation`  | Qué pantalla viene después lo decide el navigator.                |
| `OnBoardingCancelled`   | `CloseScreen`          | La vista también podría simplemente ocultar un panel.             |
| `LastUserAdded(user)`   | `ShowToast(user)`      | Podría ser un toast, un snackbar o nada.                          |

Una buena prueba: el nombre sigue teniendo sentido si reemplazas por completo la UI de la pantalla.

## Reglas de entrega

- **Una vez.** Cuando `onEvent` termina, el evento queda consumido. No se repite al recrear la vista.
- **No se pierden.** Los eventos se encolan en el ViewModel hasta que una vista puede recibirlos. Si publicas un evento
  mientras la pantalla está en segundo plano, o durante una rotación, se entrega después.
- **En orden.** Varios eventos pendientes se entregan juntos, en el orden en que se publicaron.
- **Sin duplicados por defecto.** Si ya hay un evento igual esperando, `postEvent` ignora el nuevo. Pasa
  `allowDuplicated = true` para encolarlo igualmente:

```kotlin
postEvent(LoginEvent.Message("Ana"), allowDuplicated = true)
```

- **Solo mientras la pantalla está visible.** Las pantallas Compose reciben eventos mientras el ciclo de vida está en
  `STARTED`; los Fragments y las Activities, desde `onResume` hasta `onStop`.
- **Un evento igual se vuelve a entregar después de consumir el anterior.** Publicar `Message("Ana")` dos veces, la
  segunda después de gestionar la primera, muestra el mensaje dos veces.
- **No se guardan.** Los eventos viven en el ViewModel, así que sobreviven a los cambios de configuración pero no a la
  muerte del proceso.

> **Mantén `onEvent` breve.** Si llega un evento nuevo mientras un `onEvent` anterior está suspendido, la entrega se
> reinicia y el evento sin terminar se puede volver a entregar. Haz trabajo rápido en `onEvent` y devuelve el trabajo
> largo al ViewModel como una acción.

## ¿Eventos o estado?

Si tiene que seguir ahí cuando el usuario vuelve, es [estado](state.es.md), no un evento.
Consulta la tabla de [Arquitectura](architecture.es.md#estado-o-evento).

## Pantallas sin eventos

Usa `EmaEvent.EMPTY` como tipo de evento, y `EmaViewModel.EMPTY` en una pantalla sin estado, acciones ni eventos,
como una activity que solo aloja otras pantallas.
