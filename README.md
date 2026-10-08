# Ema

**English** · [Español](README.es.md)

**Ema** is a small library for building screens with a unidirectional, MVI-style flow:
a **state** the view renders, **actions** the user performs, and one-shot **events** the view reacts to.
The ViewModels are pure Kotlin Multiplatform code, the screens are built with Jetpack Compose, and the Android View
system is supported in its own module.

```kotlin
data class CounterState(val count: Int = 0) : EmaState

sealed interface CounterAction : EmaAction {
    data object Increment : CounterAction
}

sealed interface CounterEvent : EmaEvent {
    data class LimitReached(val limit: Int) : CounterEvent
}

class CounterViewModel :
    EmaViewModelAction<CounterState, CounterAction, CounterEvent>(CounterState()) {

    override fun onStateCreated(initializer: EmaAction.Initializer?) = Unit

    override fun onAction(action: CounterAction) {
        when (action) {
            CounterAction.Increment -> {
                updateState { copy(count = count + 1) }
                if (state.count == 10) postEvent(CounterEvent.LimitReached(10))
            }
        }
    }
}
```

## Install

```kotlin
// settings.gradle.kts
maven { url = uri("https://jitpack.io") }
```

```kotlin
dependencies {
    // Compose
    implementation("com.github.carmabs.ema:ema-android:7.0.0")
    implementation("com.github.carmabs.ema:ema-compose:7.0.0")

    // Android Views
    implementation("com.github.carmabs.ema:ema-android-view:7.0.0")
}
```

For a pure Kotlin or multiplatform module use `ema-core`. Then initialize Ema in your `Application`:

```kotlin
Ema.init(EmaConfiguration.Android)
```

## Documentation

The guide is in [`docs/`](docs/index.md):

- [Getting started](docs/getting-started.md)
- Concepts: [architecture](docs/concepts/architecture.md), [state](docs/concepts/state.md), [actions](docs/concepts/actions.md), [events](docs/concepts/events.md), [ViewModel](docs/concepts/viewmodel.md)
- Guides: [configuration](docs/guides/configuration.md), [initializers](docs/guides/initializers.md), [async work and errors](docs/guides/async-and-errors.md), [testing](docs/guides/testing.md), [Kotlin Multiplatform](docs/guides/multiplatform.md) and more
- [Compose](docs/compose/screens.md): screens, navigation, back handling, dialogs, saved state and permissions
- [Android Views](docs/android-view/index.md): Fragments, Activities, dialogs and lists
- [Recommendations](docs/recommendations.md) and [contributing](docs/contributing.md)

## Sample

The [`sample/`](sample) app shows every feature working together: a login with validation and dialogs,
a list with roles, and a Compose flow with navigation, back handling and saved state.
Open the `sample` folder in Android Studio and run the `app` module.
