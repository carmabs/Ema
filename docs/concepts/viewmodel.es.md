# ViewModel

Un ViewModel de Emanate es una clase de Kotlin normal, sin dependencias de Android. Hay dos formas de escribirlo:

| Clase                         | Estilo                                                                                  |
|-------------------------------|-----------------------------------------------------------------------------------------|
| `EmaViewModel<S, A, E>`       | Declarativo: reducers, middlewares y side effects para cada tipo de acción. **Recomendado.** |
| `EmaViewModelAction<S, A, E>` | Clásico, imperativo: cada acción se gestiona en `onAction`, que actualiza el estado directamente. |

`EmaViewModel` es la opción recomendada. Su estado lo guarda una [`EmaSource`](source.es.md): cada acción pasa por
los middlewares, los reducers y los side effects de su tipo, y el estado solo cambia en los reducers, que son funciones
puras. El flujo de todas las pantallas es el mismo y predecible, la lógica de negocio se reparte en piezas pequeñas
declaradas en un solo sitio, y el ViewModel completo se puede testear grabando sus acciones, estados y eventos con
[emanate-test](../guides/testing.es.md).

Los ViewModels clásicos, `EmaViewModelAction` y `EmaViewModelClassic`, se mantienen por compatibilidad y para migrar poco
a poco las pantallas existentes. Los dos tipos pueden convivir en la misma app: las vistas solo dependen de
`EmaViewModelContract`.

## EmaViewModel

Extiende `EmaViewModel<State, Action, Event>`, pasa el estado inicial y declara en `setup` cómo se gestionan las
acciones. `source { }` crea el setup con los tipos del ViewModel, así que no hay que volver a escribirlos:

```kotlin
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    initialDataState: LoginState
) : EmaViewModel<LoginState, LoginAction, LoginEvent>(initialDataState) {

    override fun setup() = source {
        reducers {
            add<LoginAction> { action ->
                when (action) {
                    is LoginAction.UserNameWritten -> state.copy(userName = action.user, userNameError = false)
                    LoginAction.Login -> state.copy(userNameError = state.userName.isBlank())
                    // ...
                }
            }
        }
        sideEffects {
            add<LoginAction.Login>(concurrency = SideEffectConcurrency.Leading) {
                if (state.userNameError) return@add
                dispatch(LoginRequest.Started)
                loginUseCase(LoginUseCase.Input(state.userName, state.userPassword))
                    .onSuccess { user -> dispatch(LoginRequest.Succeeded(user)) }
                    .onFailure { dispatch(LoginRequest.Failed) }
            }
            add<LoginRequest.Succeeded> { action -> postEvent(LoginEvent.LoginSuccess(action.user)) }
        }
    }
}
```

| En `source { }`                    | Para qué                                                                   |
|------------------------------------|----------------------------------------------------------------------------|
| `reducers { add<Tipo> { } }`       | Devuelve el siguiente estado para las acciones de ese tipo. Puro: solo lee `state`. |
| `middlewares { add<Tipo> { } }`    | Decide el camino de las acciones de ese tipo antes de los reducers: `next()`, `nextAfter { }`... |
| `sideEffects { add<Tipo> { } }`    | Lógica de negocio que se ejecuta después de los reducers: casos de uso, `dispatch`, `postEvent`. |
| `broadcasts { listen<Tipo>() }`    | Recibe las acciones de ese tipo emitidas por otros ViewModels. Consulta [Resultados entre pantallas](../guides/results-between-screens.es.md). |

Dentro de cada bloque, `+` añade una pieza ya creada, como `+middlewareDelayOf(...)` o un reducer declarado como
propiedad.

Todo sobre reducers, middlewares y side effects está en [Source](source.es.md).

Además del estado inicial, el constructor recibe:

- `dispatcher`: dónde se ejecuta el ViewModel. Por defecto, el dispatcher `main` de la
  [configuración](../guides/configuration.es.md).
- `logger`: el `EmaLogger` que registra cada acción con el estado antes y después de ella, con el nombre del
  ViewModel como tag. Por defecto, el `logger` de la [configuración](../guides/configuration.es.md#logs), que es
  `EmaLogger.Println` si no se cambia; pasa `null` para desactivar los logs de este ViewModel.

`setup` se llama la primera vez que se usa el ViewModel, no en el constructor, así que puede usar las propiedades de tu
clase, como sus casos de uso.

### Acciones internas

Los side effects comunican sus resultados con acciones, para que los reducers los apliquen. Cuando una acción solo la
despacha el ViewModel, declárala `internal` en el mismo módulo, junto a las acciones de la pantalla, para que la vista
no pueda enviarla:

```kotlin
internal sealed interface LoginRequest : LoginAction {
    data object Started : LoginRequest
    data class Succeeded(val user: User) : LoginRequest
    data object Failed : LoginRequest
}
```

### El ciclo de vida como acciones

`EmaViewModel` no tiene ganchos del ciclo de vida: el ciclo de vida de la vista llega como acciones, que los
middlewares, los reducers y los side effects gestionan como cualquier otra.

| Acción                            | Cuándo                                                                   |
|-----------------------------------|--------------------------------------------------------------------------|
| `EmaAction.Initializer`           | **Una vez**, la primera vez que se crea la pantalla: su [inicializador](../guides/initializers.es.md), o `Initializer.EMPTY`. |
| `EmaAction.Lifecycle.Started`     | La pantalla se hace visible.                                             |
| `EmaAction.Lifecycle.Resumed`     | La pantalla está en primer plano.                                        |
| `EmaAction.Lifecycle.Paused`      | La pantalla deja de estar completamente visible.                         |
| `EmaAction.Lifecycle.Stopped`     | La pantalla pasa a segundo plano.                                        |
| `EmaAction.Lifecycle.Destroyed`   | Se destruye el ViewModel. Después se cancela su trabajo.                 |

Cargar los datos iniciales es un side effect del inicializador:

```kotlin
sideEffects {
    add<HomeInitializer> { initializer ->
        when (initializer) {
            is HomeInitializer.HomeUser -> {
                val friends = getUserFriendsUseCase(GetUserFriendsUseCase.Input(initializer.user))
                dispatch(HomeFriendsLoaded(friends))
            }
        }
    }
}
```

## ViewModels clásicos

`EmaViewModelAction<S, A, E>` recibe las acciones en `onAction` y cambia el estado de forma imperativa con
`updateState`. `EmaViewModelClassic<S, E>` no declara acciones: la vista llama a funciones públicas del ViewModel, como en
el MVVM clásico. Entre los dos, mejor `EmaViewModelAction`: su único punto de entrada, `dispatch`, y las acciones
`sealed` hacen el código más fácil de seguir.

Los dos reciben el estado inicial en el constructor y, opcionalmente, el `CoroutineDispatcher` donde se ejecuta su
trabajo (por defecto el dispatcher principal de la [configuración](../guides/configuration.es.md)). Crean su propio scope
con él y lo cancelan cuando se destruyen.

```kotlin
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    initialDataState: LoginState
) : EmaViewModelAction<LoginState, LoginAction, LoginEvent>(initialDataState) {

    override fun onStateCreated(initializer: EmaAction.Initializer?) = Unit

    override fun onAction(action: LoginAction) { /* ... */ }
}
```

### Qué puedes usar dentro

| Miembro                                       | Para qué                                                                 |
|-----------------------------------------------|--------------------------------------------------------------------------|
| `state`                                       | El estado actual (solo lectura).                                         |
| `updateState { copy(...) }`                   | Reemplaza el estado y avisa a la vista.                                  |
| `postEvent(event, allowDuplicated)`           | Envía un evento de un solo uso. Consulta [Eventos](events.es.md).        |
| `sideEffect { }` / `singleSideEffect(id) { }` | Ejecuta trabajo suspendido. Consulta [Trabajo asíncrono y errores](../guides/async-and-errors.es.md). |
| `broadcast(action, keepUntilReceived)`        | Emite una acción, por ejemplo un resultado para la pantalla anterior. Consulta [Resultados entre pantallas](../guides/results-between-screens.es.md). |
| `registerBroadcastListener<A> { }`            | Recibe las acciones emitidas de tipo `A`.                                |
| `scope`                                       | El `CoroutineScope` del ViewModel.                                       |

### Ganchos del ciclo de vida

El ViewModel sigue el ciclo de vida de su pantalla, y los ganchos le permiten reaccionar a cada etapa sin saber nada de
Android:

- **Creación**, para preparar la pantalla: leer el inicializador, cargar los datos iniciales y registrar los listeners de
  resultados. Ocurre una sola vez, así que una rotación no vuelve a cargar los datos.
- **Visibilidad**, para ejecutar trabajo solo mientras el usuario ve la pantalla: arrancar las actualizaciones de
  ubicación, un temporizador o un refresco periódico cuando se hace visible, y pararlos cuando pasa a segundo plano.
- **Destrucción**, para liberar lo que tenga el ViewModel cuando su pantalla se cierra definitivamente.

`onStateCreated` es obligatorio; sobrescribe los demás solo cuando los necesites.

| Gancho                        | Cuándo                                                                   |
|-------------------------------|--------------------------------------------------------------------------|
| `onStateCreated(initializer)` | **Una vez**, la primera vez que se crea la pantalla. Recibe el [inicializador](../guides/initializers.es.md). |
| `onBroadcastListenerSetup()`  | Justo después, para registrar los [listeners de resultados](../guides/results-between-screens.es.md). |
| `onViewStarted()`             | La pantalla se hace visible.                                             |
| `onViewResumed()`             | La pantalla está en primer plano.                                        |
| `onViewPaused()`              | La pantalla deja de estar completamente visible.                         |
| `onViewStopped()`             | La pantalla pasa a segundo plano.                                        |
| `onDestroy()`                 | Se destruye el ViewModel. El scope ya está cancelado.                    |

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

## El contrato con la vista

Las vistas no dependen de la implementación del ViewModel, sino de `EmaViewModelContract<S, E>`: el estado inicial,
`stateFlow`, `eventFlow`, `consumeEvent` y las llamadas del ciclo de vida (`onCreated`, `onStartView`, `onResumeView`,
`onPauseView`, `onStopView` y `onCleared`). `EmaViewModel`, `EmaViewModelAction` y `EmaViewModelClassic` lo implementan, y
`EmaViewModelContract.EMPTY` es el ViewModel de una pantalla sin estado, acciones ni eventos.

## Cómo se conserva el ViewModel

En Android, Emanate guarda tu ViewModel dentro de un `ViewModel` de Android (`EmaAndroidViewModel`), así que vive tanto
como su pantalla y sobrevive a los cambios de configuración:

1. La primera vez que se abre una pantalla, Emanate llama a la función que le has dado (`viewModel` en Compose,
   `provideViewModel()` en las vistas) y guarda esa instancia.
2. Mientras la pantalla existe, después de una rotación o al volver desde el back stack, Emanate reutiliza la instancia
   que guarda. Tu función no se vuelve a llamar.
3. Cuando se cierra la pantalla, el ViewModel se destruye. Si se vuelve a abrir, se crea uno nuevo.

Así, cada pantalla tiene su propio ViewModel, creado una sola vez. Consulta
[Inyección de dependencias](../guides/dependency-injection.es.md).

El ViewModel se identifica por el nombre de su clase (`id`). Cuando se destruye el `ViewModel` de Android, llama a
`onCleared`: un `EmaViewModel` procesa las acciones pendientes y `EmaAction.Lifecycle.Destroyed`, y después cancela su
trabajo; un ViewModel clásico cancela su `scope`.

## Pintar antes de la primera actualización

`updateOnInitialization` (protegido, `true` por defecto) decide si una pantalla Compose pinta el estado inicial
directamente. Sobrescríbelo con `false` para pintar solo después de la primera actualización del estado: en un
`EmaViewModel`, después de la primera acción de la pantalla o del primer inicializador distinto de `Initializer.EMPTY`.
