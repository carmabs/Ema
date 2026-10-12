# Async work and errors

In an `EmaViewModel`, suspending work runs in the side effects declared in `setup`: they are launched by the actions
of their type and report their result dispatching actions, as explained in [Source](../concepts/source.md). They
follow the same configuration as the `sideEffect` of the classic ViewModels described here: the default actions, the
exception policy and the use cases running on the background dispatcher.

In a classic ViewModel, run suspending work with `sideEffect`:

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

`sideEffect` launches a coroutine in the ViewModel `scope`, so it is cancelled automatically when the ViewModel is cleared.
By default the block runs on the main dispatcher of the scope; **use cases switch to the background dispatcher of the
[configuration](configuration.md) themselves** (`Dispatchers.IO` on Android), so you do not need to.
Pass `dispatcher` to run the block somewhere else.

## Reacting to the outcome

`sideEffect` returns a handler where you can chain callbacks:

```kotlin
sideEffect { repository.load() }
    .onSuccess { data -> updateState { copy(data = data) } }
    .onError { error -> postEvent(MyEvent.LoadFailed) }
    .onFinish { updateState { copy(isLoading = false) } }
```

`onFinish` runs whether the work succeeded or failed. The handler also exposes the `job`.

Cancellation is not an error: when the ViewModel is cleared, or `singleSideEffect` replaces the work, the coroutine is
cancelled and `onError` is not called.

## Cancelling the previous work

`singleSideEffect(id)` cancels the previous work launched with the same id. Use it for searches or any action that is
repeated quickly:

```kotlin
singleSideEffect("search") {
    delay(300)
    updateState { copy(results = searchUseCase(query)) }
}
```

## Errors

The `errorPolicy` of the [configuration](configuration.md) decides what happens when a side effect throws an
exception:

| `EmaErrorPolicy`  | Effect |
|-------------------|--------|
| `Catch(onError)`  | **Default, and in release builds with `EmaConfiguration.Android(debug)`.** The exception is caught and written to the `logger`, and the ViewModel keeps working. `onError` also receives each one. |
| `Throw`           | The exception is thrown again and the app fails at once. **In debug builds with `EmaConfiguration.Android(debug)`.** |

```kotlin
Emanate.init(
    EmaConfiguration.Android(debug = BuildConfig.DEBUG).copy(
        errorPolicy = EmaErrorPolicy.Catch { error -> crashReporter.record(error.exception) }
    )
)
```

`onError` receives an `EmaError` with the exception and its `reflection`: the name of the ViewModel and of the function
that launched the work. See [Naming the side effects](configuration.md#naming-the-side-effects).

To track every side effect, for example to measure them, set a `sideEffectListener`. Its `onSuccess` and `onFinish`
are called when each one ends:

```kotlin
EmaConfiguration.Android(debug = BuildConfig.DEBUG).copy(
    sideEffectListener = object : EmaSideEffectListener {
        override fun onFinish(reflection: Reflection) = analytics.track(reflection.methodName)
    }
)
```

`sideEffect(throwException = true) { }` rethrows the exceptions of a single side effect, whatever the policy.

For recoverable errors, **return a result instead of throwing**. `EmaResult<T, E>` is like `kotlin.Result` but the failure
can be any type: see [Utilities](utilities.md).
