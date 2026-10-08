# Ema

[English](README.md) · **Español**

**Ema** es una pequeña librería para construir pantallas con un flujo unidireccional, de estilo MVI:
un **estado** que pinta la vista, **acciones** que realiza el usuario y **eventos** de un solo uso a los que reacciona
la vista. Los ViewModels son código Kotlin Multiplatform puro, las pantallas se construyen con Jetpack Compose y el
sistema de vistas de Android tiene soporte en su propio módulo.

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

## Instalación

```kotlin
// settings.gradle.kts
maven { url = uri("https://jitpack.io") }
```

```kotlin
dependencies {
    // Compose
    implementation("com.github.carmabs.ema:ema-android:7.0.0")
    implementation("com.github.carmabs.ema:ema-compose:7.0.0")

    // Vistas de Android
    implementation("com.github.carmabs.ema:ema-android-view:7.0.0")
}
```

En un módulo de Kotlin puro o multiplataforma usa `ema-core`. Después inicializa Ema en tu `Application`:

```kotlin
Ema.init(EmaConfiguration.Android)
```

## Documentación

La guía está en [`docs/`](docs/index.es.md):

- [Primeros pasos](docs/getting-started.es.md)
- Conceptos: [arquitectura](docs/concepts/architecture.es.md), [estado](docs/concepts/state.es.md), [acciones](docs/concepts/actions.es.md), [eventos](docs/concepts/events.es.md), [ViewModel](docs/concepts/viewmodel.es.md)
- Guías: [configuración](docs/guides/configuration.es.md), [inicializadores](docs/guides/initializers.es.md), [trabajo asíncrono y errores](docs/guides/async-and-errors.es.md), [testing](docs/guides/testing.es.md), [Kotlin Multiplatform](docs/guides/multiplatform.es.md) y más
- [Compose](docs/compose/screens.es.md): pantallas, navegación, botón atrás, diálogos, estado guardado y permisos
- [Vistas de Android](docs/android-view/index.es.md): Fragments, Activities, diálogos y listas
- [Recomendaciones](docs/recommendations.es.md) y [contribuir](docs/contributing.es.md)

## Ejemplo

La app [`sample/`](sample) muestra todas las funcionalidades juntas: un login con validación y diálogos, una lista con
roles y un flujo en Compose con navegación, botón atrás y estado guardado.
Abre la carpeta `sample` en Android Studio y ejecuta el módulo `app`.
