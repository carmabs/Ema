# Configuration

Ema is configured once, when the application starts, with `Ema.init`:

```kotlin
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Ema.init(EmaConfiguration.Android)
    }
}
```

- Call it **before any ViewModel is created**: ViewModels read the configuration when they are built.
- It can be called **only once**. A second call throws `IllegalStateException`.
- If it is never called, the default `EmaConfiguration()` is used. That is the usual case in unit tests and in
  non-Android platforms.

## What can be configured

| Property                      | Default `EmaConfiguration()` | `EmaConfiguration.Android`         | Purpose                                                   |
|-------------------------------|------------------------------|------------------------------------|-----------------------------------------------------------|
| `dispatchers`                 | `EmaDispatchers()`           | `EmaDispatchers()`                 | Where ViewModels, use cases and CPU-intensive work run. See [Dispatchers](#dispatchers). |
| `sideEffectConfig`            | `EmaSideEffectConfig()`      | Resolves the method names from the lambdas | How `sideEffect` handles errors and what it reports. See [Async work and errors](async-and-errors.md#errors). |
| `dataClassPrinter`            | `EmaDataClassPrinter.ToString` | Prints every field, indented     | How `toStringPretty()` prints objects.                    |

`EmaConfiguration` is a data class, so customize the Android configuration with `copy`:

```kotlin
Ema.init(
    EmaConfiguration.Android.copy(
        sideEffectConfig = EmaConfiguration.Android.sideEffectConfig.copy(
            exceptionPolicy = EmaSideEffectConfig.ExceptionPolicy.CatchExceptions { error ->
                Log.e("Ema", "${error.reflection.methodName} failed", error.exception)
            }
        )
    )
)
```

`EmaConfiguration.Android` is in `ema-android` (`com.carmabs.ema.android.configuration.Android`).

## Dispatchers

`EmaDispatchers` groups the dispatchers Ema uses by default:

| Dispatcher | Default                                                                      | Used by                                                               |
|------------|------------------------------------------------------------------------------|-----------------------------------------------------------------------|
| `main`     | `Dispatchers.Main.immediate`, or `Dispatchers.Main` where it isn't supported | ViewModels, through their default scope `EmaMainScope()`.             |
| `io`       | `Dispatchers.IO`, or `Dispatchers.Default` on JS and Wasm                    | [Use cases](utilities.md#use-cases): network, disk and blocking work. |
| `default`  | `Dispatchers.Default`                                                        | CPU-intensive work, such as processing images in `ema-android`.       |

`main` is `Main.immediate` because the work started from the main thread, such as dispatching an action from a click,
runs at once instead of waiting for the next frame, as in the `viewModelScope` of Android.

Change any of them with `copy` or by creating your own:

```kotlin
Ema.init(EmaConfiguration.Android.copy(dispatchers = EmaDispatchers(io = myIoDispatcher)))
```

## Naming the side effects

Every `sideEffect` reports the name of the function that launched it, which is useful for logging errors.

- `EmaConfiguration.Android` reads it from the class the compiler generates for the lambda, so a `sideEffect` in
  `doLogin()` is reported as `doLogin`.
- On other platforms the name is generic. Pass `logName` to set it yourself, on any platform:

```kotlin
sideEffect(logName = "login") { loginUseCase(input) }
```

To decide the name differently, implement `EmaMethodNameResolver` and set it in `EmaSideEffectConfig.methodNameResolver`.

## Printing objects

`toStringPretty()` prints any object with the configured `EmaDataClassPrinter`. `EmaConfiguration.Android` uses
`EmaAndroidDataClassPrinter`, which prints the fields, nested objects, collections and maps with indentation.
Implement `EmaDataClassPrinter` to print them your own way.

## The broadcast manager

`Ema.broadcastManager` is the app-wide `EmaBroadcastManager`. See
[Results between screens](results-between-screens.md#app-wide-broadcasts).
