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
data.title.stringResource()                 // Compose (com.carmabs.emanate.compose.extension)
data.title.string(context)                  // Android (com.carmabs.emanate.android.extension)
```

`R.string.x.toEmaText(args)` and `R.plurals.x.toEmaText(quantity, args)` are shortcuts from `emanate-android`.

Two `EmaText` are equal when they describe the same text, arguments included, so a state that holds them is compared
correctly. A text without arguments is never formatted: `EmaText.text("50%")` is shown as is.

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
| `EmaUseCase<I, O>`             | A `suspend` operation. Runs on the background dispatcher of the [configuration](configuration.md) (`Dispatchers.IO` on Android) unless you pass another one. |
| `EmaFlowUseCase<I, O>`         | An operation that emits values over time (`Flow<O>`), collected on the background dispatcher. |
| `EmaSyncUseCase<I, O>`         | A fast, synchronous operation.                                                   |

Implement `useCaseFunction` (the protected function) and call the use case like a function: `loginUseCase(input)`.

## Pretty printing

`toStringPretty()` prints any object, for example a state, with the `printer` of the
[configuration](configuration.md#printing-objects). With `EmaConfiguration.Android(debug)` it prints every field,
nested objects and collections, indented:

```kotlin
Log.d("Login", state.toStringPretty())
```

## `EmaUtils`

The utility functions are grouped by theme in `EmaUtils`, so writing `EmaUtils.` shows them all. The Android modules
add their own functions to the themes, under the name of the platform:

| Theme                        | Module                    | Functions                                                              |
|------------------------------|---------------------------|------------------------------------------------------------------------|
| `EmaUtils.Coroutines`        | `emanate-core`            | `suspendCoroutineWithTimeout(duration) { }`, `requireMinDurationOf(duration) { }` |
| `EmaUtils.Flows`             | `emanate-core`            | `singleEvent<T>()`: a `MutableSharedFlow` with no replay, for one-shot signals. |
| `EmaUtils.Positions`         | `emanate-core`            | `iterateFromCenter(index, length)`, `iterateFromEnd(index, length)`    |
| `EmaUtils.Size`              | `emanate-core`            | `fitInTargetMaxSizeProportionally(width, height, targetWidth, targetHeight)` |
| `EmaUtils.Values`            | `emanate-core`            | `checkUpdate(old, new) { }`: runs the block only if the value changed. |
| `EmaUtils.Screen.Android`    | `emanate-android`         | `metrics(context, includeInsets)`                                      |
| `EmaUtils.Screen.Compose`    | `emanate-android-compose` | `widthDp()`, `heightDp()`, `widthPx()`, `heightPx()`                   |
| `EmaUtils.Color.Android`     | `emanate-android`         | `fromGradient(colors, positions, value)`, `lerp(colorA, colorB, t)`    |
| `EmaUtils.View.Android`      | `emanate-android-view`    | `visibility(visible, gone)`: `VISIBLE`, `GONE` or `INVISIBLE`.         |
| `EmaUtils.Preview.Compose`   | `emanate-android-compose` | `isInPreview()`, `skipForPreview(previewComposable) { }`               |

```kotlin
val user = EmaUtils.Coroutines.requireMinDurationOf(1.seconds) { loginUseCase(input) }
val metrics = EmaUtils.Screen.Android.metrics(context)
```

The platform groups, like `Android` or `Compose`, are extension properties: the IDE imports them when you autocomplete
`EmaUtils.Screen.`.

## Other helpers

| Helper                    | What it is                                                                      |
|---------------------------|---------------------------------------------------------------------------------|
| `EmaImage`                | An image described as `ByteArray`, `Uri` or resource `Id`, with size and tint.  |
| `EmaUniqueSelector`       | Keeps a single option selected in a group and tells which ones were deselected. |

`emanate-core` also has extensions for numbers, strings, lists, dates and flows in `com.carmabs.emanate.core.extension`.

## Android helpers

`emanate-android` has the Android helpers shared by Compose and Views:

| Helper                                                    | What it does                                                        |
|-----------------------------------------------------------|---------------------------------------------------------------------|
| `EmaText.string(context)`, `Int.toEmaText(...)`            | Resolve and create [texts](#ematext-strings-the-viewmodel-can-hold). |
| `Context.showToast(message, duration)` / `EmaSingleToast` | A toast that avoids overlapping when several are shown at the same time. |
| `Int.dp`, `Int.sp`                                         | Units.                                                              |
| `R.drawable.x.getDrawable(context)`, `getBitmap`, `getColor`, `getDimension`, `getFont`... | Read resources from their id. |
| `Bitmap.resizeCrop`, `resizeFitInside`, `getRoundedCornerBitmap`, `toByteArray`, `ByteArray.toBitmap` | Transform images. |
| `EmaUriRes.getResourceId(context)`, `onDrawable`, `onString`... | Resolve a resource described by its name.                      |
| `Bundle.getParcelableCompat`, `Intent.getSerializableExtraCompat`... | Read extras on every Android version.                   |
| `Intent.setInitializer`, `Activity.getInitializer`          | [Initializers](initializers.md) in intents.                        |
| `ComponentActivity.addOnBackPressedListener { }`            | Handle the back button with an `EmaBackHandlerStrategy`.            |
| `Context.findActivity()`, `findComponentActivity()`         | Find the activity behind a context.                                 |
