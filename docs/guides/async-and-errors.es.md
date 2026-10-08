# Trabajo asíncrono y errores

Ejecuta trabajo suspendido en el ViewModel con `sideEffect`:

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

**Por defecto, las excepciones lanzadas dentro de `sideEffect` se capturan y se ignoran**, salvo que uses `onError` o
configures una acción por defecto. Elige una política en el `sideEffectConfig` de la [configuración](configuration.es.md):

```kotlin
Ema.init(
    EmaConfiguration.Android.copy(
        sideEffectConfig = EmaConfiguration.Android.sideEffectConfig.copy(
            exceptionPolicy = EmaSideEffectConfig.ExceptionPolicy.CatchExceptions(
                defaultAction = { error -> Log.e("Ema", "${error.reflection.methodName} failed", error.exception) }
            ),
            defaultSuccessAction = { success -> /* por ejemplo, analítica */ },
            defaultFinishAction = { reflection -> /* ... */ }
        )
    )
)
```

| Opción                                | Efecto                                                                         |
|---------------------------------------|--------------------------------------------------------------------------------|
| `CatchExceptions(defaultAction)`      | **Por defecto.** Las excepciones se capturan. Se llama a `defaultAction` con cada una. |
| `ThrowExceptions`                     | Las excepciones se relanzan y la app falla. Útil mientras desarrollas.         |
| `defaultSuccessAction`                | Se llama cada vez que un `sideEffect` termina bien.                            |
| `defaultFinishAction`                 | Se llama cada vez que un `sideEffect` termina.                                 |

Los callbacks reciben un `EmaReflection` (o `EmaReflectionData` / `EmaReflectionException`) con el nombre del ViewModel y
de la función que lanzó el trabajo, útil para los logs. Consulta [Nombrar los side effects](configuration.es.md#nombrar-los-side-effects).

`sideEffect(throwException = true) { }` relanza las excepciones de un único side effect, sea cual sea la política.

Para los errores recuperables, **devuelve un resultado en vez de lanzar una excepción**. `EmaResult<T, E>` es como
`kotlin.Result`, pero el fallo puede ser de cualquier tipo: consulta [Utilidades](utilities.es.md).
