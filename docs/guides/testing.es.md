# Testing

Como un ViewModel no tiene dependencias de Android, se testea como Kotlin normal: sin emulador y sin Robolectric.
Si tus ViewModels viven en un módulo multiplataforma, los mismos tests se ejecutan en todos los targets desde `commonTest`.
Todo el contrato de estado y eventos es observable:

- `dispatch(action)` es la entrada.
- `stateFlow.value` es el estado.
- `eventFlow` contiene los eventos pendientes.

## Testear un EmaViewModel con emanate-test

`emanate-test` tiene las utilidades para testear los ViewModels hechos con `EmaViewModel` y las
[sources](../concepts/source.es.md). Es una librería JVM, así que añádela a los tests de un módulo JVM o Android:

```kotlin
dependencies {
    testImplementation("com.github.carmabs.ema:emanate-test:$emanateVersion")
}
```

Incluye `kotlinx-coroutines-test` y necesita la misma versión de `emanate-core` con la que se compiló.

| Utilidad                        | Para qué                                                                      |
|---------------------------------|-------------------------------------------------------------------------------|
| `useTestDispatchers()`          | Ejecuta los ViewModels, las sources y los casos de uso creados después en el dispatcher del test, también como `Dispatchers.Main`. Llámalo antes de cada test. |
| `resetTestDispatchers()`        | Restaura los dispatchers y la configuración de Emanate. Llámalo después de cada test. |
| `viewModel.record()`            | Graba en orden las acciones, los estados y los eventos de un ViewModel o una source. |
| `assertActions(...)`            | Las acciones grabadas, incluidas las que bloquea un middleware.               |
| `assertStates(...)`             | Todos los estados que devuelven los reducers.                                 |
| `assertState(state)` / `assertState { }` | El último estado, o una condición sobre él.                          |
| `assertEvents(...)` / `assertNoEvents()` | Los eventos publicados.                                              |

```kotlin
class LoginViewModelTest {

    @BeforeTest
    fun setUp() {
        useTestDispatchers()
    }

    @AfterTest
    fun tearDown() {
        resetTestDispatchers()
    }

    @Test
    fun `the right credentials log in the user`() = runTest {
        val viewModel = LoginViewModel(LoginUseCase(FakeRepository()), LoginState.DEFAULT)
        val recorder = viewModel.record()

        viewModel.dispatch(LoginAction.UserNameWritten("Admin"))
        viewModel.dispatch(LoginAction.PasswordWritten("1234"))
        viewModel.dispatch(LoginAction.Login)
        advanceUntilIdle()

        recorder.assertState { !it.isLoading }
        recorder.assertEvents(LoginEvent.Message("Carmabs"), LoginEvent.LoginSuccess(admin))
    }
}
```

- **Crea el ViewModel después de `useTestDispatchers`**, para que se ejecute en el dispatcher del test. Por defecto es
  un `UnconfinedTestDispatcher`, que procesa las acciones en cuanto se despachan; pasa un `StandardTestDispatcher` para
  procesarlas cuando avance el test.
- **Graba antes de la primera acción** para grabarlo todo.
- **`advanceUntilIdle()`** ejecuta los side effects, retardos incluidos, porque `runTest` usa el scheduler del
  dispatcher del test.
- **Se pueden testear varios ViewModels** juntos, por ejemplo los resultados emitidos entre pantallas: el
  `LoginViewModelTest` del sample crea los ViewModels de creación de perfil, home y login, y comprueba que el usuario
  creado en el primero llega al último.

## Testear los ViewModels clásicos

Añade `kotlinx-coroutines-test` a tus dependencias de test:

```kotlin
class CounterViewModelTest {

    private fun TestScope.createViewModel() =
        CounterViewModel(UnconfinedTestDispatcher(testScheduler)).apply {
            onCreated()
        }

    @Test
    fun `increment action updates the state`() = runTest {
        val viewModel = createViewModel()

        viewModel.dispatch(CounterAction.Increment)

        assertEquals(CounterState(count = 1), viewModel.stateFlow.value)
    }

    @Test
    fun `reaching the limit posts an event`() = runTest {
        val viewModel = createViewModel()

        repeat(10) { viewModel.dispatch(CounterAction.Increment) }

        assertEquals(
            listOf<CounterEvent>(CounterEvent.LimitReached(10)),
            viewModel.eventFlow.first()
        )
    }
}
```

Los tests de la propia librería, en `emanate-core/src/commonTest`, tienen más ejemplos: side effects, eventos, resultados
entre pantallas y el ciclo de vida de la vista.

### Lo que conviene saber

1. **Pasa tu propio dispatcher.** El segundo parámetro del constructor de `EmaViewModelClassic` / `EmaViewModelAction`
   es el `CoroutineDispatcher` donde se ejecuta el ViewModel, que crea su propio scope con él. Exponlo en el constructor
   de tus ViewModels, con un valor por defecto, para que los tests puedan sustituirlo. El de por defecto es el
   dispatcher `main` de la configuración, `Dispatchers.Main`, que no existe en los tests unitarios. Con un `UnconfinedTestDispatcher` el trabajo se ejecuta inmediatamente; con un
   `StandardTestDispatcher`, llama a `advanceUntilIdle()` para ejecutarlo.

   ```kotlin
   class CounterViewModel(dispatcher: CoroutineDispatcher = Emanate.configuration.dispatchers.main) :
       EmaViewModelAction<CounterState, CounterAction, CounterEvent>(CounterState(), dispatcher)
   ```
2. **No hace falta configuración.** Si no se llama a `Emanate.init`, se usa la configuración por defecto. No llames a
   `Emanate.init` en los tests: solo se puede llamar una vez por proceso.
3. **Llama a `onCreated()`** para ejecutar `onStateCreated(initializer)`. Pasa un inicializador si la pantalla lo
   necesita: `viewModel.onCreated(MyInitializer.Default("x"))`.
4. **Usa casos de uso falsos.** Son clases normales: pasa una implementación falsa, o usa tu librería de mocks.
5. **La vista consume los eventos.** En un test, léelos con `eventFlow.first()` y llama a `consumeEvent(event)` para
   simular que la vista los ha gestionado.
6. **El tiempo.** El dispatcher de test se salta los `delay` dentro de un `sideEffect`, así que los debounces y los
   timeouts se ejecutan al instante.

## Qué testear

| Test                                                 | Cómo                                                       |
|------------------------------------------------------|------------------------------------------------------------|
| Una acción cambia el estado                          | `dispatch` y comprueba `stateFlow.value`.                  |
| Una acción publica un evento                         | `dispatch` y comprueba `eventFlow.first()`.                |
| Un fallo muestra el diálogo o error correcto         | Caso de uso falso que falla; comprueba el campo del estado. |
| Se aplica el inicializador                           | `onCreated(initializer)` y comprueba el estado.            |
| Las propiedades derivadas del estado                 | Tests unitarios normales de la data class.                 |

## Testear las pantallas

El contenido de una pantalla Compose no tiene estado: `onState(state, actions)` solo pinta lo que recibe. Píntalo con
cualquier estado (consulta [Previews](../compose/screens.es.md#previews)) y compruébalo con tests de UI de Compose o con
tests de capturas, sin ViewModel.
