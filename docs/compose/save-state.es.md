# Guardar el estado si el proceso muere

Android puede matar tu app mientras está en segundo plano, por ejemplo cuando el usuario va a los ajustes del sistema a
conceder un permiso. Los argumentos de navegación (el [inicializador](../guides/initializers.es.md)) se restauran solos,
pero lo que el usuario ha escrito no.

Un `EmaSaveStateManager` conecta una pantalla con un `SavedStateHandle`. Se ejecuta una vez por pantalla y te da:

- un `CoroutineScope` ligado a la pantalla,
- el `SavedStateHandle` para leer y escribir,
- el ViewModel.

Restaura **enviando acciones** y guarda **recogiendo el estado**:

```kotlin
createComposableScreen(
    screenContent = ProfileCreationScreenContent(),
    viewModel = { get<ProfileCreationViewModel>() },
    saveStateManager = EmaSaveStateManager<ProfileCreationState, ProfileCreationEvent> { coroutineScope, savedStateHandle, emaViewModel ->
        val keyName = "USERNAME"

        // Restaurar
        savedStateHandle.get<String>(keyName)?.also {
            emaViewModel.asActionDispatcher<ProfileCreationAction>()
                .dispatch(ProfileCreationAction.UserNameWritten(it))
        }

        // Guardar
        coroutineScope.launch {
            emaViewModel.stateFlow.collect {
                savedStateHandle[keyName] = it.name
            }
        }
    },
    onEvent = { /* ... */ }
)
```

Restaurar mediante acciones hace que el ViewModel valide y aplique los datos exactamente igual que si el usuario los
hubiera vuelto a escribir. `asActionDispatcher<A>()` te da el ViewModel como un `EmaActionDispatcher<A>`.

## Sin grafo de navegación

Cuando usas `EmaComposableScreen` directamente, pasa un `SavedStateSupport(savedStateHandle, manager)` como `saveStateSupport`.

## Consejos

- Guarda solo lo que molestaría perder al usuario: campos de formularios, elementos seleccionados. Un `SavedStateHandle`
  es para valores pequeños.
- No guardes el estado entero. Los datos que se pueden volver a cargar (listas de la red) deberían cargarse de nuevo en
  `onStateCreated`.
