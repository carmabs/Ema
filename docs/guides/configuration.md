# Configuration

Emanate has a configuration for the whole application, set once, when it starts, with `Emanate.init`:

```kotlin
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Emanate.init(EmaConfiguration.Android(debug = BuildConfig.DEBUG))
    }
}
```

- Call it **before any ViewModel is created**: ViewModels read the configuration when they are built.
- It can be called **only once**. A second call throws `IllegalStateException`.
- If it is never called, the default `EmaConfiguration()` is used. That is the usual case in unit tests and in
  non-Android platforms.

A ViewModel or a source can also receive its own configuration, which is used instead of the one of the application.
See [Explicit configuration](#explicit-configuration).

## What can be configured

Each property configures one thing:

| Property             | Default `EmaConfiguration()`   | `EmaConfiguration.Android(debug)`             | Purpose |
|----------------------|--------------------------------|-----------------------------------------------|---------|
| `dispatchers`        | `EmaDispatchers()`             | `main` is `Dispatchers.Main.immediate`        | Where ViewModels, use cases and CPU-intensive work run. See [Dispatchers](#dispatchers). |
| `errorPolicy`        | `EmaErrorPolicy.Catch()`       | `Throw` in debug, `Catch()` in release        | What happens when a middleware, reducer or side effect throws an exception. See [Errors](async-and-errors.md#errors). |
| `logger`             | `EmaLogger.Println`            | `EmaLogger.Logcat` in debug, `null` in release | Where the logs of Emanate go. See [Logs](#logs). |
| `printer`            | `EmaDataClassPrinter.ToString` | Prints every field, indented                  | How objects are printed in the logs and by `toStringPretty()`. |
| `destroyedTimeout`   | 5 seconds                      | 5 seconds                                     | How long the side effects of `Lifecycle.Destroyed` can run after the ViewModel is cleared, unless they have their own `timeout`. |
| `sideEffectListener` | `null`                         | `null`                                        | Receives the result of every side effect, to track or measure them. |
| `methodNameResolver` | Generic names                  | Resolved from the lambdas                     | The name of the function that launched a side effect, reported in the errors. See [Naming the side effects](#naming-the-side-effects). |
| `broadcaster`        | `EmaActionBroadcaster.Shared`  | `EmaActionBroadcaster.Shared`                 | Sends the actions broadcast between ViewModels. See [Libraries and SDKs](#libraries-and-sdks). |

`EmaConfiguration.Android(debug)` is in `emanate-android` (`com.carmabs.emanate.android.configuration.Android`). Pass
`BuildConfig.DEBUG`: debug builds log to logcat and fail at once with any exception, and release builds don't log, so
the state doesn't reach logcat, and keep working after an exception.

`EmaConfiguration` is a data class, so change only what you need with `copy`:

```kotlin
Emanate.init(
    EmaConfiguration.Android(debug = BuildConfig.DEBUG).copy(
        errorPolicy = EmaErrorPolicy.Catch { error -> crashReporter.record(error.exception) }
    )
)
```

## Dispatchers

`EmaDispatchers` groups the dispatchers Emanate uses by default:

| Dispatcher | Default                                                   | Used by                                                               |
|------------|-----------------------------------------------------------|-----------------------------------------------------------------------|
| `main`     | `Dispatchers.Main`                                        | ViewModels and sources, by default.                                   |
| `io`       | `Dispatchers.IO`, or `Dispatchers.Default` on JS and Wasm | [Use cases](utilities.md#use-cases): network, disk and blocking work. |
| `default`  | `Dispatchers.Default`                                     | CPU-intensive work, such as processing images in `emanate-android`.   |

`EmaConfiguration.Android(debug)` uses `Dispatchers.Main.immediate` as `main`, as the `viewModelScope` of Android does: the
work started from the main thread, such as dispatching an action from a click, runs at once instead of waiting for the
next frame.

Change any of them with `copy`, keeping the others:

```kotlin
val android = EmaConfiguration.Android(debug = BuildConfig.DEBUG)
Emanate.init(android.copy(dispatchers = android.dispatchers.copy(io = myIoDispatcher)))
```

## Naming the side effects

Every `sideEffect` reports the name of the function that launched it, which is useful for logging errors.

- `EmaConfiguration.Android(debug)` reads it from the class the compiler generates for the lambda, so a `sideEffect` in
  `doLogin()` is reported as `doLogin`.
- On other platforms the name is generic. Pass `logName` to set it yourself, on any platform:

```kotlin
sideEffect(logName = "login") { loginUseCase(input) }
```

To decide the name differently, implement `EmaMethodNameResolver` and set it in `EmaConfiguration.methodNameResolver`.

## Printing objects

`toStringPretty()` prints any object with the `printer` of the configuration. `EmaConfiguration.Android(debug)` uses
`EmaAndroidDataClassPrinter`, which prints the fields, nested objects, collections and maps with indentation.
Implement `EmaDataClassPrinter` to print them your own way.

## Logs

Emanate writes its logs with the `logger` of the configuration. By default it is `EmaLogger.Println`, which writes to
the standard output and adds `EMA_` before the tag. `EmaConfiguration.Android(debug)` uses `EmaLogger.Logcat` in debug
builds, which writes with `Log.d`, and no logger in release builds. The errors caught by the
[error policy](async-and-errors.md#errors) are also logged.

Every `EmaViewModel` uses it, with its name as tag, to log its actions:

- One line when an action is received, before the middlewares: `Received action: Search(query=ema)`. It shows the
  actions that a middleware stops, which don't reach the reducers.
- A block with the action and the state before and after its reducers. It is written right before them, after all the
  middlewares, so the state after the action is correct even if a middleware continues it later with `nextAfter`, and
  the action is the one the reducers receive.

The state may contain data of the user, so don't log in release builds, as `EmaConfiguration.Android(debug)` does. To
send the logs to your own logger:

```kotlin
Emanate.init(
    EmaConfiguration.Android(debug = BuildConfig.DEBUG).copy(
        logger = if (BuildConfig.DEBUG) EmaLogger { tag, message -> Timber.tag(tag).d(message) } else null
    )
)
```

A ViewModel can still pass its own `logger` in the constructor, or `null` to disable only its logs. A source used on
its own doesn't log: add `loggerMiddleware(tag, logger)` as its last middleware.

## Explicit configuration

`Emanate.configuration` is shared by the whole process. `EmaViewModel`, `EmaViewModelClassic`, `EmaViewModelAction` and
`EmaSource(...)` read it when they are created, as the default value of their `configuration` parameter, which goes
right after the initial state. Pass one to use it instead:

```kotlin
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    configuration: EmaConfiguration = Emanate.configuration
) : EmaViewModel<LoginState, LoginAction, LoginEvent>(LoginState.DEFAULT, configuration) {
    // ...
}
```

The `dispatcher` and `logger` parameters take their default value from it, and can still be passed by name to change
only them. The configuration is kept by the ViewModel and its source, so its error policy, side effect listener,
`destroyedTimeout` and broadcaster are the ones of the configuration they received.

**Use `Emanate.init`** in an application: it configures every ViewModel in one place, and they don't need any
parameter. **Pass the configuration** when the one of the application isn't enough or can't be relied on:

- **Libraries and SDKs.** `Emanate.init` can be called only once and belongs to the application. A library built with
  Emanate passes its own configuration to its ViewModels, so it doesn't clash with an application that also uses
  Emanate, nor depend on whether it was initialized. See [Libraries and SDKs](#libraries-and-sdks).
- **A module with a different configuration**, for example its own logger or error policy.
- **Dependency injection.** The configuration is one more dependency, provided by Hilt or Koin. See
  [Dependency injection](dependency-injection.md#injecting-the-configuration).
- **Tests.** Each test passes its configuration, with the dispatchers of the test, without changing the one of Emanate,
  so the tests can run in parallel. See [Testing](testing.md#without-changing-the-configuration-of-emanate).

The [use cases](utilities.md#use-cases) don't receive the whole configuration, only a dispatcher in the constructor:
by default the `io` dispatcher of the configuration of Emanate, so pass the `io` dispatcher of yours. `toStringPretty()`
prints with the `printer` of the configuration of Emanate unless it receives another one.

## Libraries and SDKs

A library built with Emanate can be used by an application that also uses Emanate and calls `Emanate.init`. To keep
them apart, the library doesn't call `Emanate.init` nor use the configuration of Emanate: it creates its own and passes
it to all its pieces.

```kotlin
internal object MyLibraryEmanate {
    val configuration = EmaConfiguration.Android(debug = BuildConfig.DEBUG).copy(
        logger = if (BuildConfig.DEBUG) EmaLogger(prefix = "MYLIB_") { tag, message -> Log.d(tag, message) } else null,
        broadcaster = EmaActionBroadcaster()
    )
}

internal class PayUseCase(private val repository: PaymentRepository) :
    EmaUseCase<Payment, Receipt>(MyLibraryEmanate.configuration.dispatchers.io) {
    // ...
}

internal class PaymentViewModel(private val payUseCase: PayUseCase) :
    EmaViewModel<PaymentState, PaymentAction, PaymentEvent>(PaymentState(), MyLibraryEmanate.configuration) {
    // ...
}
```

- **ViewModels and sources** receive the configuration of the library as their `configuration` parameter.
- **Use cases** receive its `io` dispatcher.
- **Printing objects:** `toStringPretty(MyLibraryEmanate.configuration.printer)`.
- **Broadcast actions.** With its own `EmaActionBroadcaster`, the ViewModels of the library only receive the actions
  broadcast by each other, and the ones of the application don't receive theirs. Keep the shared one,
  `EmaActionBroadcaster.Shared`, if the library and the application send results to each other.

If the application must be able to change something, like the logger, add a function to the library that receives it
before any of its screens is opened, instead of using `Emanate.init`.

The application and the library use the same `emanate-core`: the version that Gradle resolves for both. Keep the library
on a version compatible with the applications that use it.
