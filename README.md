# Ema

**Ema** is a small Android library for building screens with a unidirectional, MVI-style flow:
a **state** the view renders, **actions** the user performs, and one-shot **events** the view reacts to.
It works with Views and Jetpack Compose, uses Kotlin coroutines and Koin, and keeps the ViewModel free of Android code.

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
    implementation("com.github.carmabs.ema:ema-android:7.0.0")
    implementation("com.github.carmabs.ema:ema-compose:7.0.0") // optional, for Compose
}
```

For a pure Kotlin module use `ema-core`.

## Documentation

The guide is in [`docs/`](docs/index.md):

- [Getting started](docs/getting-started.md)
- Concepts: [architecture](docs/concepts/architecture.md), [state](docs/concepts/state.md), [actions](docs/concepts/actions.md), [events](docs/concepts/events.md), [ViewModel](docs/concepts/viewmodel.md)
- Guides: [XML views](docs/guides/xml-views.md), [Compose](docs/guides/compose.md), [navigation](docs/guides/navigation.md), [back handling](docs/guides/back-handling.md), [dialogs](docs/guides/dialogs.md), [testing](docs/guides/testing.md) and more
- [Recommendations](docs/recommendations.md)
- [Migrating from 6.x](docs/migration-from-6.md)

## Sample

The [`sample/`](sample) app shows every feature working together: a login with validation and dialogs,
a list with roles, and a Compose flow with navigation, back handling and saved state.
Open the `sample` folder in Android Studio and run the `app` module.
