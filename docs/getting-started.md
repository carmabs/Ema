# Getting started

## Requirements

- Android `minSdk` 24 or higher.
- The library is built with Java 21 and Kotlin 2.
- [Koin](https://insert-koin.io/) is used for dependency injection and is already included in `ema-android`.

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

Add the dependencies:

```kotlin
dependencies {
    implementation("com.github.carmabs.ema:ema-android:7.0.0")
    implementation("com.github.carmabs.ema:ema-compose:7.0.0") // only if you use Compose
}
```

For a pure Kotlin module (for example a `presentation` module with no Android code) add only the core:

```kotlin
dependencies {
    implementation("com.github.carmabs.ema:ema-core:7.0.0")
}
```

## Initialize Ema

Extend `EmaApplication` and register your Koin modules. Do not call `startKoin` yourself:
Ema starts it for you, with its own module included.

```kotlin
class MyApplication : EmaApplication() {

    override val emaConfiguration = EmaApplicationConfig()

    override fun KoinApplication.injectAppModules(): List<Module> =
        listOf(dataModule, useCaseModule, uiModule)
}
```

Register the application in `AndroidManifest.xml` (`android:name=".MyApplication"`).

If your application already extends another class, implement `EmaApplicationAware` instead and call `initializeEma` from `onCreate`:

```kotlin
class MyApplication : SomeOtherApplication(), EmaApplicationAware {

    override fun onCreate() {
        super.onCreate()
        initializeEma(EmaApplicationConfig())
    }

    override fun KoinApplication.injectAppModules(): List<Module> =
        listOf(dataModule, useCaseModule, uiModule)
}
```

`EmaApplication` is just a convenience that implements `EmaApplicationAware` for you.
`EmaApplicationConfig` lets you customise how errors inside `sideEffect` are handled;
see [Async work and errors](guides/async-and-errors.md).

> **Warning:** `EmaApplicationConfig` can only be initialized once per process. Ema reads it when a ViewModel is created,
> so a ViewModel created before `initializeEma` runs will fail.

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

Register it in Koin as a `factory`:

```kotlin
val uiModule = module {
    factoryOf(::CounterViewModel)
}
```

### With Views (XML)

```kotlin
class CounterFragment :
    EmaFragment<CounterFragmentBinding, CounterState, CounterViewModel, CounterEvent>() {

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = CounterFragmentBinding.inflate(inflater, container, false)

    override fun provideViewModel(): CounterViewModel = injectDirect()

    override val navigator: EmaNavigator<CounterEvent>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bIncrement.setOnClickListener {
            viewModel.dispatch(CounterAction.Increment)
        }
    }

    override fun CounterFragmentBinding.onState(state: CounterState) {
        bindForUpdate(state::count) {
            tvCount.text = it.toString()
        }
    }

    override suspend fun CounterFragmentBinding.onEvent(event: CounterEvent) {
        when (event) {
            is CounterEvent.LimitReached ->
                Toast.makeText(requireContext(), "Limit: ${event.limit}", Toast.LENGTH_SHORT).show()
        }
    }
}
```

The fragment must live inside an activity that uses a Koin scope, as every Ema `Activity` does.
More in [Views with XML](guides/xml-views.md).

### With Compose

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

And add it to your navigation graph:

```kotlin
NavHost(
    navController = navController,
    startDestination = CounterScreenContent::class.routeId
) {
    createComposableScreen(
        screenContent = CounterScreenContent(),
        viewModel = { injectDirect<CounterViewModel>() },
        onEvent = { event ->
            // Navigation decisions go here
        }
    )
}
```

More in [Compose](guides/compose.md).

## What just happened

1. The user taps the button: the view calls `dispatch(Increment)`.
2. The ViewModel computes the next state with `updateState`.
3. The view receives the new state and `bindForUpdate` updates the text, only if `count` changed.
4. On the tenth tap the ViewModel also posts `LimitReached`, which the view shows once and consumes.

Continue with [Architecture](concepts/architecture.md).
