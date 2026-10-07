# Saving state across process death

Android can kill your app while it is in the background, for example when the user goes to the system settings to
grant a permission. Navigation arguments (the [initializer](initializers.md)) are restored automatically, but the state
the user has typed is not.

An `EmaSaveStateManager` connects a screen to a `SavedStateHandle`. It runs once for the screen, and gives you:

- the `SavedStateHandle` to read and write,
- a `CoroutineScope` tied to the screen,
- the ViewModel.

Restore by **dispatching actions**, and save by **collecting the state**:

```kotlin
createComposableScreen(
    screenContent = ProfileCreationScreenContent(),
    viewModel = { injectDirect<ProfileCreationViewModel>() },
    saveStateManager = EmaSaveStateManager<ProfileCreationState, ProfileCreationEvent> { coroutineScope, savedStateHandle, emaViewModel ->
        val keyName = "USERNAME"

        // Restore
        savedStateHandle.get<String>(keyName)?.also {
            emaViewModel.asActionDispatcher<ProfileCreationAction>()
                .dispatch(ProfileCreationAction.UserNameWritten(it))
        }

        // Save
        coroutineScope.launch {
            emaViewModel.stateFlow.collect {
                savedStateHandle[keyName] = it.name
            }
        }
    },
    onEvent = { /* ... */ }
)
```

Restoring through actions means the ViewModel validates and applies the data exactly as if the user had typed it again.

## Where to use it

`saveStateManager` is a parameter of `createComposableScreen`. When you use `EmaComposableScreen` directly, pass a
`SavedStateSupport(savedStateHandle, manager)` as `saveStateSupport`.

## Tips

- Save only what the user would be upset to lose: form fields, selected items. A `SavedStateHandle` is for small values.
- Do not save the whole state. Data you can reload (lists from the network) should be loaded again in `onStateCreated`.
- Use `asActionDispatcher<A>()` on the ViewModel to get an `EmaActionDispatcher<A>`.
