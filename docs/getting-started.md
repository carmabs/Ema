# Getting started

## Requirements

- Android `minSdk` 24 or higher.
- The library is built with Java 21 and Kotlin 2.

## Install

Add the JitPack repository:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependencies of your UI technology:

```kotlin
val emaVersion = "7.0.0"

dependencies {
    // Compose (recommended)
    implementation("com.github.carmabs.ema:ema-android:$emaVersion")
    implementation("com.github.carmabs.ema:ema-compose:$emaVersion")

    // Android Views: Fragments and Activities
    implementation("com.github.carmabs.ema:ema-android-view:$emaVersion")
}
```

For a pure Kotlin module, like a `presentation` module with the ViewModels, add only the core:

```kotlin
dependencies {
    implementation("com.github.carmabs.ema:ema-core:$emaVersion")
}
```

`ema-core` is a Kotlin Multiplatform library, so it can also be used from the `commonMain` of a multiplatform module.
See [Kotlin Multiplatform](guides/multiplatform.md).

## Initialize Ema

Call `Ema.init` once, when the application starts and before any ViewModel is created.
`EmaConfiguration.Android` is the configuration for Android apps:

```kotlin
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Ema.init(EmaConfiguration.Android)
    }
}
```

Register the application in `AndroidManifest.xml` (`android:name=".MyApplication"`).
The configuration decides the dispatchers, how errors inside `sideEffect` are handled and how objects are printed
in the logs. See [Configuration](guides/configuration.md).

## Your first screen

A screen is made of a **state**, the **actions** the user can perform, the **events** it can emit
and a **ViewModel** that connects them. This is a counter.

```kotlin
data class CounterState(val count: Int = 0) : EmaState

sealed interface CounterAction : EmaAction.Screen {
    data object Increment : CounterAction
}

sealed interface CounterEvent : EmaEvent {
    data class LimitReached(val limit: Int) : CounterEvent
}

class CounterViewModel :
    EmaViewModelAction<CounterState, CounterAction, CounterEvent>(CounterState()) {

    override fun onStateCreated(initializer: EmaInitializer?) = Unit

    override fun onAction(action: CounterAction) {
        when (action) {
            CounterAction.Increment -> {
                updateState { copy(count = count + 1) }
                if (state.count == LIMIT) postEvent(CounterEvent.LimitReached(LIMIT))
            }
        }
    }

    private companion object {
        const val LIMIT = 10
    }
}
```

- `updateState { copy(...) }` creates the next state and notifies the view.
- `postEvent(...)` sends a one-shot event to the view.
- The ViewModel never touches Android classes, so it can be tested on the JVM.

### The screen

A Compose screen is described by an `EmaComposableScreenContent`:

```kotlin
class CounterScreenContent :
    EmaComposableScreenContent<CounterState, CounterAction, CounterEvent> {

    @Composable
    override fun onState(
        state: CounterState,
        actions: EmaImmutableActionDispatcher<CounterAction>
    ) {
        Column {
            Text(text = state.count.toString())
            Button(onClick = { actions.dispatch(CounterAction.Increment) }) {
                Text(text = "+")
            }
        }
    }

    override suspend fun onEvent(
        context: Context,
        event: CounterEvent,
        actions: EmaImmutableActionDispatcher<CounterAction>
    ) {
        when (event) {
            is CounterEvent.LimitReached ->
                Toast.makeText(context, "Limit: ${event.limit}", Toast.LENGTH_SHORT).show()
        }
    }
}
```

And it is added to the navigation graph of an activity:

```kotlin
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = CounterScreenContent::class.routeId
            ) {
                createComposableScreen(
                    screenContent = CounterScreenContent(),
                    viewModel = { CounterViewModel() },
                    onEvent = { event ->
                        // Navigation decisions go here
                    }
                )
            }
        }
    }
}
```

`viewModel` is a lambda that creates the ViewModel. Use your dependency injection framework there, or create it
directly as above: see [Dependency injection](guides/dependency-injection.md). More in [Compose screens](compose/screens.md).

If you build the UI with Fragments and Activities, the same screen is written in
[Android Views](android-view/screens.md).

## What just happened

1. The user taps the button: the screen calls `actions.dispatch(Increment)`.
2. The ViewModel computes the next state with `updateState`.
3. The screen receives the new state and recomposes.
4. On the tenth tap the ViewModel also posts `LimitReached`, which the screen shows once and consumes.

Continue with [Architecture](concepts/architecture.md).
