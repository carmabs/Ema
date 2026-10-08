# Primeros pasos

## Requisitos

- `minSdk` 24 o superior en Android.
- La librería se compila con Java 21 y Kotlin 2.

## Instalación

Añade el repositorio de JitPack:

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

Añade las dependencias de tu tecnología de UI:

```kotlin
val emaVersion = "7.0.0"

dependencies {
    // Compose (recomendado)
    implementation("com.github.carmabs.ema:ema-android:$emaVersion")
    implementation("com.github.carmabs.ema:ema-compose:$emaVersion")

    // Vistas de Android: Fragments y Activities
    implementation("com.github.carmabs.ema:ema-android-view:$emaVersion")
}
```

En un módulo de Kotlin puro, como un módulo `presentation` con los ViewModels, añade solo el núcleo:

```kotlin
dependencies {
    implementation("com.github.carmabs.ema:ema-core:$emaVersion")
}
```

`ema-core` es una librería Kotlin Multiplatform, así que también se puede usar desde el `commonMain` de un módulo
multiplataforma. Consulta [Kotlin Multiplatform](guides/multiplatform.es.md).

## Inicializar Ema

Llama a `Ema.init` una vez, cuando arranca la aplicación y antes de crear ningún ViewModel.
`EmaConfiguration.Android` es la configuración para apps Android:

```kotlin
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Ema.init(EmaConfiguration.Android)
    }
}
```

Registra la aplicación en el `AndroidManifest.xml` (`android:name=".MyApplication"`).
La configuración decide los dispatchers, cómo se gestionan los errores dentro de `sideEffect` y cómo se imprimen los
objetos en los logs. Consulta [Configuración](guides/configuration.es.md).

## Tu primera pantalla

Una pantalla se compone de un **estado**, las **acciones** que puede realizar el usuario, los **eventos** que puede emitir
y un **ViewModel** que los conecta. Esto es un contador.

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
                if (state.count == LIMIT) postEvent(CounterEvent.LimitReached(LIMIT))
            }
        }
    }

    private companion object {
        const val LIMIT = 10
    }
}
```

- `updateState { copy(...) }` crea el siguiente estado y avisa a la vista.
- `postEvent(...)` envía un evento de un solo uso a la vista.
- El ViewModel nunca toca clases de Android, así que se puede testear en la JVM.

### La pantalla

Una pantalla Compose se describe con un `EmaComposableScreenContent`:

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
                Toast.makeText(context, "Límite: ${event.limit}", Toast.LENGTH_SHORT).show()
        }
    }
}
```

Y se añade al grafo de navegación de una activity:

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
                        // Aquí van las decisiones de navegación
                    }
                )
            }
        }
    }
}
```

`viewModel` es una lambda que crea el ViewModel. Usa ahí tu framework de inyección de dependencias, o créalo directamente
como arriba: consulta [Inyección de dependencias](guides/dependency-injection.es.md). Más en
[Pantallas de Compose](compose/screens.es.md).

Si construyes la UI con Fragments y Activities, la misma pantalla se escribe como en
[Vistas de Android](android-view/screens.es.md).

## Qué ha pasado

1. El usuario pulsa el botón: la pantalla llama a `actions.dispatch(Increment)`.
2. El ViewModel calcula el siguiente estado con `updateState`.
3. La pantalla recibe el nuevo estado y se recompone.
4. En la décima pulsación el ViewModel además publica `LimitReached`, que la pantalla muestra una vez y consume.

Sigue con [Arquitectura](concepts/architecture.es.md).
