# Compose

Compose screens are described by an `EmaComposableScreenContent` and registered in your navigation graph with
`createComposableScreen`.

## Screen content

```kotlin
class ProfileOnBoardingScreenContent :
    BaseScreenComposable<ProfileOnBoardingState, ProfileOnBoardingActions, ProfileOnBoardingEvent>() {

    @Composable
    override fun onState(
        state: ProfileOnBoardingState,
        actions: EmaImmutableActionDispatcher<ProfileOnBoardingActions>
    ) {
        // Draw the state. Send user input with actions.dispatch(...)
    }
}
```

`EmaComposableScreenContent<S, A, E>` has three members:

| Member                                      | Purpose                                                                  |
|---------------------------------------------|--------------------------------------------------------------------------|
| `onState(state, actions)`                   | **Required.** Draws the state. It is a `@Composable`.                    |
| `suspend onEvent(context, event, actions)`  | Handles [events](../concepts/events.md) that are UI concerns (messages, haptics...). Optional. |
| `onBack(state): A?`                         | The action to send when back is pressed, or `null`. See [Back handling](back-handling.md). Optional. |

Keep the screen content free of state: it is a stateless description, created once per screen.

## Registering the screen

```kotlin
NavHost(
    navController = navController,
    startDestination = ProfileOnBoardingScreenContent::class.routeId
) {
    createComposableScreen(
        screenContent = ProfileOnBoardingScreenContent(),
        viewModel = { injectDirect<ProfileOnBoardingViewModel>() },
        initializerSupport = EmaInitializerSupport.kSerialization(
            ProfileOnBoardingInitializer.serializer(),
            getInitializer(
                BundleSerializerStrategy.kSerialization(ProfileOnBoardingInitializer.serializer()),
                savedInstanceState
            )
        ),
        onEvent = { navigator.handleProfileOnBoardingEvent(it) }
    )
}
```

| Parameter              | Purpose                                                                                        |
|------------------------|------------------------------------------------------------------------------------------------|
| `screenContent`        | The screen content.                                                                            |
| `viewModel`            | A lambda that creates the ViewModel (usually from Koin).                                       |
| `onEvent`              | Receives every event. Put **navigation** here, where the `NavController` is available.         |
| `routeId`              | The route. By default `ScreenContent::class.routeId`, which is unique per class.               |
| `initializerSupport`   | How to read the [initializer](initializers.md) from the route arguments.                       |
| `saveStateManager`     | See [Saving state across process death](save-state.md).                                        |
| `transitionAnimation`  | `EmaComposableTransitions(enterTransition, exitTransition, popEnterTransition, popExitTransition)`. |
| `fullScreenDialogMode` | Registers the destination as a `dialog` instead of a `composable`.                             |
| `decoration`           | Wraps the screen, for example in a `Scaffold`. Receives the content and the action dispatcher. |
| `previewRenderState`   | State to draw in the IDE preview.                                                              |
| `onViewModelInstance`  | Gives you the ViewModel instance, for example to share it with other composables.              |

## Two places to handle events

Each event reaches **both** handlers, in this order:

1. The `onEvent` lambda of `createComposableScreen` (no context, but your `NavController` is at hand).
2. `screenContent.onEvent(context, event, actions)`.

Then the event is consumed. A simple rule: navigation in the graph, everything else in the screen content.

## Navigating

Routes are strings. Navigate with the extension that also carries an initializer:

```kotlin
navController.navigate(
    route = ProfileCreationScreenContent::class.routeId,
    initializerBundle = EmaInitializerBundle(
        ProfileCreationInitializer.Admin,
        BundleSerializerStrategy.kSerialization(ProfileCreationInitializer.serializer())
    )
)
```

More in [Navigation](navigation.md).

## Dialogs

Dialogs are drawn from the state. Emit the dialog composable when the state says so:

```kotlin
state.overlap?.also { Overlap(it, actions) }
```

See [Dialogs](dialogs.md).

## Previews

Inside the IDE preview there is no ViewModel, so call `onState` directly with a state and an empty dispatcher:

```kotlin
@Preview
@Composable
private fun NormalPreview() {
    EmaSampleTheme {
        onState(
            state = ProfileCreationState(Role.ADMIN, "Carlos", "Mateo"),
            actions = EmaImmutableActionDispatcherEmpty()
        )
    }
}
```

`previewRenderState` in `createComposableScreen` does the same for the whole screen. `isInPreview()` and
`skipForPreview { }` help you skip code that cannot run in a preview.

## Lifecycle

`createComposableScreen` connects the ViewModel to the lifecycle of the destination: `onCreated`, `onStartView`,
`onResumeView`, `onPauseView` and `onStopView` are called from the matching lifecycle events. The state is collected
with `collectAsStateWithLifecycle` and the events while the lifecycle is `STARTED`.

## Using a screen without a navigation graph

`EmaComposableScreen` is the composable behind `createComposableScreen`. Use it to place an Ema screen anywhere:

```kotlin
EmaComposableScreen(
    vm = { injectDirect<CounterViewModel>() },
    screenContent = CounterScreenContent(),
    onEvent = { /* ... */ }
)
```

## Hosting Compose in a Fragment or Activity

If you prefer to stay in the Fragment/Activity world, extend `EmaComposableFragment` or `EmaComposableActivity`.
They behave like [`EmaFragment` and `EmaActivity`](xml-views.md) but draw a composable:

```kotlin
class HomeComposeFragment : EmaComposableFragment<HomeState, HomeViewModel, HomeEvent>() {

    @Composable
    override fun onRenderState(state: HomeState) { /* ... */ }

    override suspend fun onEvent(event: HomeEvent) { /* ... */ }

    override fun provideViewModel() = injectDirect<HomeViewModel>()

    override val navigator: EmaNavigator<HomeEvent>? = null

    override val initializerStrategy = BundleSerializerStrategy.EMPTY
}
```

## Theme

Wrap the `NavHost` (or your content) in your Material theme. The sample uses `EmaSampleTheme`, which reads the same
palette resources as its XML theme so both worlds look the same.
