# Async work and errors

Run suspending work in the ViewModel with `sideEffect`:

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
By default the block runs on the main dispatcher of the scope; **use cases switch to the IO dispatcher themselves**,
so you do not need to.

## Reacting to the outcome

`sideEffect` returns a handler where you can chain callbacks:

```kotlin
sideEffect { repository.load() }
    .onSuccess { data -> updateState { copy(data = data) } }
    .onError { error -> postEvent(MyEvent.LoadFailed) }
    .onFinish { updateState { copy(isLoading = false) } }
```

`onFinish` runs whether the work succeeded or failed. The handler also exposes the `job`.

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

**By default exceptions thrown inside `sideEffect` are caught and ignored** unless you use `onError`
or configure a default action. Choose a policy in `EmaApplicationConfig`:

```kotlin
class MyApplication : EmaApplication() {

    override val emaConfiguration = EmaApplicationConfig(
        sideEffectConfig = EmaSideEffectConfig(
            exceptionPolicy = EmaSideEffectConfig.ExceptionPolicy.CatchExceptions(
                defaultAction = { error -> Log.e("Ema", "${error.reflection.methodName} failed", error.exception) }
            ),
            defaultSuccessAction = { success -> /* e.g. analytics */ },
            defaultFinishAction = { reflection -> /* ... */ }
        )
    )
}
```

| Option                                | Effect                                                                         |
|---------------------------------------|--------------------------------------------------------------------------------|
| `CatchExceptions(defaultAction)`      | **Default.** Exceptions are caught. `defaultAction` is called for each one.    |
| `ThrowExceptions`                     | Exceptions are rethrown and crash the app. Handy while developing.             |
| `defaultSuccessAction`                | Called every time a `sideEffect` succeeds.                                     |
| `defaultFinishAction`                 | Called every time a `sideEffect` ends.                                         |

The callbacks receive an `EmaReflection` (or `EmaReflectionData` / `EmaReflectionException`) with the name of the ViewModel and
the function that launched the work, useful for logging.

For recoverable errors, **return a result instead of throwing**. `EmaResult<T, E>` is like `kotlin.Result` but the failure
can be any type: see [Utilities](utilities.md).
