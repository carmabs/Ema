# Utilities

## `EmaText`: strings the ViewModel can hold

The state must not contain `Context`, so it cannot call `getString`. `EmaText` describes a text and is resolved by the view.

```kotlin
EmaText.text("Hello")                       // a literal
EmaText.id(R.string.welcome, userName)      // a string resource with arguments
EmaText.plural(R.plurals.friends, count)    // a plural resource
EmaText.composition(EmaText.id(R.string.a), EmaText.text(" "), EmaText.id(R.string.b))  // concatenation
EmaText.empty()
```

Resolve it where you have a context:

```kotlin
data.title.string(requireContext())         // Views
data.title.stringResource()                 // Compose (import com.carmabs.ema.compose.extension.stringResource)
```

`R.string.x.toEmaText(args)` and `R.plurals.x.toEmaText(quantity, args)` are shortcuts for Views code.

## `EmaResult<T, E>`

Like `kotlin.Result`, but the failure can be **any type**, not only `Throwable`. Use it as the return type of use cases
so failures are part of the contract:

```kotlin
class LoginUseCase(private val repository: Repository) :
    EmaUseCase<LoginUseCase.Input, EmaResult<User, LoginException>>() {

    override suspend fun useCaseFunction(input: Input) =
        repository.login(LoginRequest(input.username, input.password))

    data class Input(val username: String, val password: String)
}
```

```kotlin
result
    .onSuccess { user -> /* ... */ }
    .onFailure { error -> /* ... */ }
```

| Function                                           | Does                                         |
|----------------------------------------------------|----------------------------------------------|
| `EmaResult.success(data)` / `EmaResult.failure(e)` | Creates a result.                            |
| `isSuccess` / `isFailure`                          | Tells which one it is.                       |
| `getOrNull()`, `getOrDefault(x)`, `getOrThrow()`, `getFailureOrNull()` | Reads it.                |
| `map`, `flatMap`                                   | Transform the success.                       |
| `mapError`, `flatMapError`                         | Transform the failure.                       |
| `onSuccess`, `onFailure`                           | Side effects, returning the same result.     |

## Use cases

| Class                          | Use it for                                                                       |
|--------------------------------|----------------------------------------------------------------------------------|
| `EmaUseCase<I, O>`             | A `suspend` operation. Runs on `Dispatchers.IO` unless you pass another dispatcher. |
| `EmaFlowUseCase<I, O>`         | An operation that emits values over time (`Flow<O>`), collected on IO.           |
| `EmaSyncUseCase<I, O>`         | A fast, synchronous operation.                                                   |

Implement `useCaseFunction` (the protected function) and call the use case like a function: `loginUseCase(input)`.

## Other helpers

| Helper                  | What it is                                                                  |
|-------------------------|-----------------------------------------------------------------------------|
| `EmaImage`              | An image described as `ByteArray`, `Uri` or resource `Id`, with size and tint. |
| `EmaUniqueSelector`     | Keeps a single option selected in a group and tells which ones were deselected. |
| `emaFlowSingleEvent<T>()` | A `MutableSharedFlow` with no replay, for one-shot signals.               |
| `EmaSingleToast.show(...)` | A toast that avoids overlapping when several are launched at the same time.                 |
