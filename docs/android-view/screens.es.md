# Fragments y Activities

`EmaFragment` y `EmaActivity` usan **ViewBinding** y son genéricos sobre el binding, el estado, el ViewModel y el evento.

```kotlin
class CounterFragment :
    EmaFragment<CounterFragmentBinding, CounterState, CounterViewModel, CounterEvent>() {

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = CounterFragmentBinding.inflate(inflater, container, false)

    override fun provideViewModel(): CounterViewModel = CounterViewModel()

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
                Toast.makeText(requireContext(), "Límite: ${event.limit}", Toast.LENGTH_SHORT).show()
        }
    }
}
```

## Lo que implementas

| Miembro                         | Fragment | Activity | Notas                                                                 |
|---------------------------------|:--------:|:--------:|-----------------------------------------------------------------------|
| `createViewBinding`             | ✔        | ✔        | Infla el layout.                                                      |
| `provideViewModel()`            | ✔        | ✔        | Crea el ViewModel. Se llama una vez, al abrir la pantalla. Consulta [Inyección de dependencias](../guides/dependency-injection.es.md). |
| `navigator`                     | ✔        | ✔        | `null` si la pantalla nunca navega. Consulta [Navegación](navigation.es.md). |
| `B.onState(state)`              | ✔        | ✔        | Pinta el estado.                                                      |
| `B.onEvent(event)`              | opcional | opcional | Gestiona los [eventos](../concepts/events.es.md). Por defecto no hace nada. |
| `initializerStrategy`           | opcional | ✔        | Cómo leer el [inicializador](../guides/initializers.es.md). `EmaFragment` y `EmaToolbarActivity` no leen ninguno por defecto. |

`onState` y `onEvent` son funciones de extensión sobre el binding, así que puedes usar las vistas directamente, sin
`binding.`. `isFirstNormalExecution` indica si `onState` está pintando el primer estado de la vista.

## Renderizar con `bindForUpdate`

`onState` se llama con cada estado nuevo. Actualizar todas las vistas cada vez es un desperdicio y puede dar problemas
(por ejemplo, reiniciar el cursor de un campo de texto). `bindForUpdate` ejecuta un bloque solo cuando ha cambiado un
campo:

```kotlin
override fun LoginFragmentBinding.onState(state: LoginState) {
    bindForUpdate(state::userNameError) {
        tilLoginUser.error = if (it) getString(R.string.login_error_user_empty) else null
    }
    bindForUpdate(state::isLoading) {
        pbLoginSign.isVisible = it
    }
}
```

- La primera vez que se pinta una vista, se ejecutan todos los bloques. Después, un bloque solo se ejecuta si su campo
  ha cambiado.
- Cuando se recrea la vista (rotación, vuelta desde el back stack) se vuelve a ejecutar todo.
- El argumento tiene que ser una referencia a una propiedad del **mismo parámetro `state`**: `state::userName`.
- Para objetos con su propia noción de igualdad, pasa `areEqualComparator`.
- `bindForUpdateWithPrevious` te da además el valor anterior.

> `bindForUpdate` encuentra el valor anterior por reflexión, a partir del nombre del campo. Si activas R8 o ProGuard,
> conserva los campos de tus clases de estado (por ejemplo,
> `-keepclassmembers class * implements com.carmabs.ema.core.state.EmaState { *; }`).

## Enviar acciones

Llama a `viewModel.dispatch(...)`:

```kotlin
bLoginSign.setOnClickListener { viewModel.dispatch(LoginAction.Login) }
```

## Campos de texto

Guarda el texto en el estado, para que sobreviva a una rotación. Cada pulsación se convierte en una acción, el
ViewModel guarda el texto y el nuevo estado vuelve a `onState`:

```kotlin
etUser.addTextChangedListener {
    viewModel.dispatch(LoginAction.UserNameWritten(it?.toString() ?: STRING_EMPTY))
}
```

Cuando vuelve el estado, escríbelo en el campo **solo si es distinto**. Escribir el mismo texto llevaría el cursor al
final mientras el usuario edita en mitad del texto:

```kotlin
bindForUpdate(state::userName) {
    if (etUser.text?.toString() != it) {
        etUser.setTextWithCursorAtEnd(it)
    }
}
```

La comprobación es también lo que hace funcionar las acciones de "borrar el campo": cuando el ViewModel vacía el texto
del estado, el campo se reescribe porque ya no coincide. `EmaEditText` hace la comprobación por ti, consulta
[Utilidades](utilities.es.md).

## Ciclo de vida

| Callback de la vista | Lo que hace Ema                                                                 |
|----------------------|---------------------------------------------------------------------------------|
| `onViewCreated` (Fragment) / `onCreate` (Activity) | `viewModel.onCreated(initializer)`                |
| `onStart`            | `viewModel.onStartView()`                                                       |
| `onResume`           | Empieza a recoger el estado y los eventos (una vez) y llama a `viewModel.onResumeView()`. |
| `onPause`            | `viewModel.onPauseView()`                                                       |
| `onStop`             | Deja de recoger y llama a `viewModel.onStopView()`.                             |

El estado se recoge en `onResume` y no antes porque los diálogos y los fragments no se pueden mostrar de forma segura
antes de que se haya restaurado el estado guardado.

### Arrancar el ViewModel más tarde

Para arrancar el ViewModel solo cuando algo esté listo, por ejemplo después de una animación, sobrescribe `startTrigger`
con un `EmaViewModelTrigger`. Las llamadas del ciclo de vida esperan hasta que se llama a `startViewModel()`, y entonces
se ejecutan en orden:

```kotlin
override val startTrigger = EmaViewModelTrigger()

private fun onIntroAnimationEnd() = startTrigger.startViewModel()
```

## Compartir un ViewModel entre fragments

Por defecto, el ViewModel de un Fragment pertenece al Fragment. Sobrescribe `fragmentViewModelScope` para asociarlo a la
Activity:

```kotlin
override val fragmentViewModelScope = false
```

Los Fragments de la misma Activity que usan la misma clase de ViewModel reciben entonces la misma instancia.

## Activities

Una Activity que solo aloja fragments usa `EmaViewModel.EMPTY` y un navigator dueño del grafo de navegación:

```kotlin
class SplashActivity :
    EmaToolbarActivity<SplashActivityBinding, EmaState.EMPTY, EmaViewModel.EMPTY, EmaEvent.EMPTY>() {

    override fun createViewBinding(inflater: LayoutInflater) = SplashActivityBinding.inflate(inflater)

    override fun provideViewModel() = EmaViewModel.EMPTY

    override fun SplashActivityBinding.onState(data: EmaState.EMPTY) = Unit

    override val navigator: EmaNavigator<EmaEvent.EMPTY> = EmaActivityNavControllerHost(
        this,
        R.id.navHostFragment,
        R.navigation.main_graph
    )

    override fun SplashActivityBinding.provideToolbar() = tbSplash
    override fun SplashActivityBinding.provideToolbarLayout() = ablSplash
}
```

`EmaToolbarActivity` añade una toolbar ligada al grafo de navegación: el título sale de la etiqueta del destino y el botón
de subir vuelve atrás. Expone `hideToolbar()`, `showToolbar()` y `setToolbarTitle()`; sobrescribe
`provideFixedToolbarTitle()` para usar el mismo título en todos los destinos.

`overridePopTransitionAnimations()` define las animaciones que se usan al cerrar la activity.

## Clases base

La mayoría de apps añaden un `BaseFragment` con los diálogos y los mensajes que necesitan todas las pantallas.
Consulta [Recomendaciones](../recommendations.es.md#crea-clases-base).
