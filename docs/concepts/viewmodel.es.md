# ViewModel

Un ViewModel de Ema es una clase de Kotlin normal, sin dependencias de Android. Hay dos variantes:

| Clase                                  | Úsala cuando                                                             |
|----------------------------------------|--------------------------------------------------------------------------|
| `EmaViewModelAction<S, A, E>`          | La vista informa de lo que ha hecho el usuario con acciones. **Recomendada.** |
| `EmaViewModelBasic<S, E>`              | Prefieres no declarar acciones: la vista llama a funciones públicas del ViewModel, como en el MVVM clásico. |

`EmaViewModelAction` es la opción recomendada. Cada entrada de la pantalla es una acción de un tipo `sealed`, así que el
compilador comprueba que se gestionan todas, el ViewModel tiene un único punto de entrada (`dispatch`) y las acciones
describen lo que ha hecho el usuario, lo que hace el código más fácil de seguir y de depurar: un log o un breakpoint en
`onAction` muestra todas las entradas. Con `EmaViewModelBasic` la vista puede llamar a cualquier función pública, y esa
estructura depende solo de la disciplina.

Las dos reciben el estado inicial en el constructor y, opcionalmente, el `CoroutineScope` donde se ejecuta su trabajo
(por defecto uno en el dispatcher principal de la [configuración](../guides/configuration.es.md)).

```kotlin
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    initialDataState: LoginState
) : EmaViewModelAction<LoginState, LoginAction, LoginEvent>(initialDataState) {

    override fun onStateCreated(initializer: EmaAction.Initializer?) = Unit

    override fun onAction(action: LoginAction) { /* ... */ }
}
```

## Qué puedes usar dentro

| Miembro                                 | Para qué                                                                 |
|-----------------------------------------|--------------------------------------------------------------------------|
| `state`                                 | El estado actual (solo lectura).                                         |
| `updateState { copy(...) }`             | Reemplaza el estado y avisa a la vista.                                  |
| `postEvent(event, allowDuplicated)`     | Envía un evento de un solo uso. Consulta [Eventos](events.es.md).        |
| `sideEffect { }` / `singleSideEffect(id) { }` | Ejecuta trabajo suspendido. Consulta [Trabajo asíncrono y errores](../guides/async-and-errors.es.md). |
| `dispatchBroadcast(data)`               | Envía un resultado a la pantalla anterior. Consulta [Resultados entre pantallas](../guides/results-between-screens.es.md). |
| `registerBackBroadcastListener(...)`    | Recibe un resultado de otra pantalla.                                    |
| `scope`                                 | El `CoroutineScope` del ViewModel.                                       |

## Ganchos del ciclo de vida

Los métodos del ciclo de vida de las clases base son `final`. Sobrescribe en su lugar los ganchos protegidos:

| La vista llama a  | Gancho que sobrescribes   | Cuándo                                                                  |
|-------------------|---------------------------|-------------------------------------------------------------------------|
| `onCreated`       | `onStateCreated(initializer)` | **Una vez**, la primera vez que se crea la pantalla. Recibe el [inicializador](../guides/initializers.es.md). |
| `onCreated`       | `onBroadcastListenerSetup()`  | Justo después, para registrar los [listeners de resultados](../guides/results-between-screens.es.md). |
| `onStartView`     | `onViewStarted()`         | La pantalla se hace visible.                                            |
| `onResumeView`    | `onViewResumed()`         | La pantalla está en primer plano.                                       |
| `onPauseView`     | `onViewPaused()`          | La pantalla deja de estar completamente visible.                        |
| `onStopView`      | `onViewStopped()`         | La pantalla pasa a segundo plano.                                       |
| `onCleared`       | `onDestroy()`             | Se destruye el ViewModel. El scope ya está cancelado.                   |

`onStateCreated` es el sitio adecuado para empezar a cargar datos. Se ejecuta una vez, no en cada rotación:

```kotlin
override fun onStateCreated(initializer: EmaAction.Initializer?) {
    val homeUser = (initializer as HomeInitializer.HomeUser).user
    sideEffect {
        val friends = getUserFriendsUseCase(GetUserFriendsUseCase.Input(homeUser))
        updateState { copy(userList = friends) }
    }
}
```

## Cómo se conserva el ViewModel

En Android, Ema guarda tu ViewModel dentro de un `ViewModel` de Android (`EmaAndroidViewModel`), así que vive tanto como
su pantalla y sobrevive a los cambios de configuración:

1. La primera vez que se abre una pantalla, Ema llama a la función que le has dado (`viewModel` en Compose,
   `provideViewModel()` en las vistas) y guarda esa instancia.
2. Mientras la pantalla existe, después de una rotación o al volver desde el back stack, Ema reutiliza la instancia que
   guarda. Tu función no se vuelve a llamar.
3. Cuando se cierra la pantalla, el ViewModel se destruye. Si se vuelve a abrir, se crea uno nuevo.

Así, cada pantalla tiene su propio ViewModel, creado una sola vez. Consulta
[Inyección de dependencias](../guides/dependency-injection.es.md).

El ViewModel se identifica por el nombre de su clase (`id`). Su `scope` se sustituye por el `viewModelScope` del
`ViewModel` de Android, así que el trabajo se cancela automáticamente cuando se destruye.

## Pintar antes de la primera actualización

`updateOnInitialization` (protegido, `true` por defecto) decide si una pantalla Compose pinta el estado inicial
directamente. Sobrescríbelo con `false` para pintar solo después de la primera llamada a `updateState`.

## Imprimir objetos

`toStringPretty()` imprime cualquier objeto, por ejemplo un estado, con el `EmaDataClassPrinter` de la
[configuración](../guides/configuration.es.md#imprimir-objetos). Con `EmaConfiguration.Android` imprime todos los campos,
los objetos anidados y las colecciones, con sangría:

```kotlin
Log.d("Login", state.toStringPretty())
```
