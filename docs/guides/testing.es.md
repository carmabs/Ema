# Testing

Como un ViewModel no tiene dependencias de Android, se testea como Kotlin normal: sin emulador y sin Robolectric.
Si tus ViewModels viven en un módulo multiplataforma, los mismos tests se ejecutan en todos los targets desde `commonTest`.
Todo el contrato de estado y eventos es observable:

- `dispatch(action)` es la entrada.
- `stateFlow.value` es el estado.
- `eventFlow` contiene los eventos pendientes.

Añade `kotlinx-coroutines-test` a tus dependencias de test:

```kotlin
class CounterViewModelTest {

    private fun TestScope.createViewModel() =
        CounterViewModel(CoroutineScope(UnconfinedTestDispatcher(testScheduler))).apply {
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

Los tests de la propia librería, en `ema-core/src/commonTest`, tienen más ejemplos: side effects, eventos, resultados
entre pantallas y el ciclo de vida de la vista.

## Lo que conviene saber

1. **Pasa tu propio scope.** El segundo parámetro del constructor de `EmaViewModelBasic` / `EmaViewModelAction` es el
   `CoroutineScope`. Exponlo en el constructor de tus ViewModels, con un valor por defecto, para que los tests puedan
   sustituirlo. El scope por defecto usa `Dispatchers.Main`, que no existe en los tests unitarios. Con un
   `UnconfinedTestDispatcher` el trabajo se ejecuta inmediatamente; con un `StandardTestDispatcher`, llama a
   `advanceUntilIdle()` para ejecutarlo.

   ```kotlin
   class CounterViewModel(scope: CoroutineScope = EmaMainScope()) :
       EmaViewModelAction<CounterState, CounterAction, CounterEvent>(CounterState(), scope)
   ```
2. **No hace falta configuración.** Si no se llama a `Ema.init`, se usa la configuración por defecto. No llames a
   `Ema.init` en los tests: solo se puede llamar una vez por proceso.
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
