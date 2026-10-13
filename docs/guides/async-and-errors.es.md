# Trabajo asíncrono y errores

En un `EmaViewModel`, el trabajo suspendido se ejecuta en los side effects declarados en `setup`: los lanzan las acciones
de su tipo y comunican su resultado despachando acciones, como se explica en [Source](../concepts/source.es.md). Siguen
la misma configuración que el `sideEffect` de los ViewModels clásicos que se describe aquí: las acciones por defecto, la
política de excepciones y los casos de uso ejecutándose en el dispatcher de segundo plano.

En un ViewModel clásico, ejecuta trabajo suspendido con `sideEffect`:

```kotlin
private fun doLogin() {
    sideEffect {
        updateState { copy(isLoading = true) }
        val result = loginUseCase(LoginUseCase.Input(state.userName, state.userPassword))
        updateState { copy(isLoading = false) }
        result
            .onSuccess { user ->
                postEvent(LoginEvent.Message(user.name))
                postEvent(LoginEvent.LoginSuccess(user))
            }
            .onFailure {
                updateState { copy(overlap = LoginOverlap.ErrorBadCredentials) }
            }
    }
}
```

`sideEffect` lanza una corrutina en el `scope` del ViewModel, así que se cancela automáticamente cuando el ViewModel se
destruye. Por defecto el bloque se ejecuta en el dispatcher principal del scope; **los casos de uso cambian ellos mismos
al dispatcher de segundo plano de la [configuración](configuration.es.md)** (`Dispatchers.IO` en Android), así que no
tienes que hacerlo tú. Pasa `dispatcher` para ejecutar el bloque en otro sitio.

## Reaccionar al resultado

`sideEffect` devuelve un manejador al que puedes encadenar callbacks:

```kotlin
sideEffect { repository.load() }
    .onSuccess { data -> updateState { copy(data = data) } }
    .onError { error -> postEvent(MyEvent.LoadFailed) }
    .onFinish { updateState { copy(isLoading = false) } }
```

`onFinish` se ejecuta tanto si el trabajo termina bien como si falla. El manejador también expone el `job`.

La cancelación no es un error: cuando el ViewModel se destruye, o `singleSideEffect` sustituye el trabajo, la corrutina
se cancela y no se llama a `onError`.

## Cancelar el trabajo anterior

`singleSideEffect(id)` cancela el trabajo anterior lanzado con el mismo id. Úsalo en búsquedas o en cualquier acción que
se repita rápido:

```kotlin
singleSideEffect("search") {
    delay(300)
    updateState { copy(results = searchUseCase(query)) }
}
```

## Errores

La `errorPolicy` de la [configuración](configuration.es.md) decide qué pasa cuando un side effect lanza una excepción, y
también un middleware o un reducer de un `EmaViewModel` o una `EmaSource`. Con `Catch`, la acción de un middleware o un
reducer que falla se descarta, sin cambiar el estado, y las siguientes acciones se procesan con normalidad:

| `EmaErrorPolicy`  | Efecto |
|-------------------|--------|
| `Catch(onError)`  | **Por defecto, y en las builds de release con `EmaConfiguration.Android(debug)`.** La excepción se captura y se escribe en el `logger`, y el ViewModel sigue funcionando. `onError` también recibe cada una. |
| `Throw`           | La excepción se relanza y la app falla en el momento. **En las builds de debug con `EmaConfiguration.Android(debug)`.** |

```kotlin
Emanate.init(
    EmaConfiguration.Android(debug = BuildConfig.DEBUG).copy(
        errorPolicy = EmaErrorPolicy.Catch { error -> crashReporter.record(error.exception) }
    )
)
```

`onError` recibe un `EmaError` con la excepción y su `reflection`: el nombre del ViewModel y de la función que lanzó el
trabajo. Consulta [Nombrar los side effects](configuration.es.md#nombrar-los-side-effects).

Para seguir cada side effect, por ejemplo para medirlos, asigna un `sideEffectListener`. Se llama a su `onSuccess` y su
`onFinish` cuando termina cada uno:

```kotlin
EmaConfiguration.Android(debug = BuildConfig.DEBUG).copy(
    sideEffectListener = object : EmaSideEffectListener {
        override fun onFinish(reflection: Reflection) = analytics.track(reflection.methodName)
    }
)
```

En los ViewModels clásicos también se aplica a `onAction`, a los métodos del ciclo de vida, como `onStateCreated` u
`onViewResumed`, y a los listeners de `registerBroadcastListener`, que siguen recibiendo después de un error.
`onAction` cambia el estado de forma imperativa, así que con `Catch` se mantienen los cambios hechos antes de la
excepción. La vista llama directamente a las funciones públicas de un `EmaViewModelClassic`, así que Emanate no puede
capturar sus excepciones: lanza con `sideEffect` el trabajo que pueda fallar.

`sideEffect(throwException = true) { }` relanza las excepciones de un único side effect, sea cual sea la política.

Para los errores recuperables, **devuelve un resultado en vez de lanzar una excepción**. `EmaResult<T, E>` es como
`kotlin.Result`, pero el fallo puede ser de cualquier tipo: consulta [Utilidades](utilities.es.md).
