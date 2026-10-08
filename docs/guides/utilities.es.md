# Utilidades

## `EmaText`: textos que puede guardar el ViewModel

El estado no puede contener un `Context`, así que no puede llamar a `getString`. `EmaText` describe un texto y la vista
lo resuelve.

```kotlin
EmaText.text("Hola")                        // un literal
EmaText.id(R.string.welcome, userName)      // un string de recursos con argumentos
EmaText.plural(R.plurals.friends, count)    // un plural de recursos
EmaText.composition(EmaText.id(R.string.a), EmaText.text(" "), EmaText.id(R.string.b))  // concatenación
EmaText.empty()
```

Resuélvelo donde tengas un contexto:

```kotlin
data.title.stringResource()                 // Compose (com.carmabs.ema.compose.extension)
data.title.string(context)                  // Android (com.carmabs.ema.android.extension)
```

`R.string.x.toEmaText(args)` y `R.plurals.x.toEmaText(quantity, args)` son atajos de `ema-android`.

Dos `EmaText` son iguales cuando describen el mismo texto, argumentos incluidos, así que un estado que los contenga se
compara correctamente. Un texto sin argumentos nunca se formatea: `EmaText.text("50%")` se muestra tal cual.

## `EmaResult<T, E>`

Como `kotlin.Result`, pero el fallo puede ser de **cualquier tipo**, no solo `Throwable`. Úsalo como tipo de retorno de
los casos de uso, para que los fallos formen parte del contrato:

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

| Función                                            | Qué hace                                     |
|----------------------------------------------------|----------------------------------------------|
| `EmaResult.success(data)` / `EmaResult.failure(e)` | Crea un resultado.                           |
| `isSuccess` / `isFailure`                          | Indica cuál de los dos es.                   |
| `getOrNull()`, `getOrDefault(x)`, `getOrThrow()`, `getFailureOrNull()` | Lo lee.                  |
| `map`, `flatMap`                                   | Transforman el éxito.                        |
| `mapError`, `flatMapError`                         | Transforman el fallo.                        |
| `onSuccess`, `onFailure`                           | Efectos secundarios; devuelven el mismo resultado. |

## Casos de uso

| Clase                          | Para                                                                             |
|--------------------------------|----------------------------------------------------------------------------------|
| `EmaUseCase<I, O>`             | Una operación `suspend`. Se ejecuta en el dispatcher de segundo plano de la [configuración](configuration.es.md) (`Dispatchers.IO` en Android), salvo que pases otro. |
| `EmaFlowUseCase<I, O>`         | Una operación que emite valores a lo largo del tiempo (`Flow<O>`), recogida en el dispatcher de segundo plano. |
| `EmaSyncUseCase<I, O>`         | Una operación rápida y síncrona.                                                 |

Implementa `useCaseFunction` (la función protegida) y llama al caso de uso como si fuera una función: `loginUseCase(input)`.

## Imprimir objetos

`toStringPretty()` imprime cualquier objeto, por ejemplo un estado, con el `EmaDataClassPrinter` de la
[configuración](configuration.es.md#imprimir-objetos). Con `EmaConfiguration.Android` imprime todos los campos,
los objetos anidados y las colecciones, con sangría:

```kotlin
Log.d("Login", state.toStringPretty())
```

## Otras ayudas

| Ayuda                     | Qué es                                                                          |
|---------------------------|---------------------------------------------------------------------------------|
| `EmaImage`                | Una imagen descrita como `ByteArray`, `Uri` o `Id` de recurso, con tamaño y tinte. |
| `EmaUniqueSelector`       | Mantiene una sola opción seleccionada en un grupo e indica cuáles se han deseleccionado. |
| `emaFlowSingleEvent<T>()` | Un `MutableSharedFlow` sin replay, para señales de un solo uso.                 |

`ema-core` también tiene extensiones para números, strings, listas, fechas y flows en `com.carmabs.ema.core.extension`.

## Ayudas de Android

`ema-android` tiene las ayudas de Android que comparten Compose y las vistas:

| Ayuda                                                      | Qué hace                                                            |
|------------------------------------------------------------|---------------------------------------------------------------------|
| `EmaText.string(context)`, `Int.toEmaText(...)`            | Resuelven y crean [textos](#ematext-textos-que-puede-guardar-el-viewmodel). |
| `Context.showToast(message, duration)` / `EmaSingleToast`  | Un toast que evita solaparse cuando se muestran varios a la vez.    |
| `Int.dp`, `Int.sp`, `getScreenMetrics(context)`            | Unidades y tamaño de la pantalla.                                   |
| `R.drawable.x.getDrawable(context)`, `getBitmap`, `getColor`, `getDimension`, `getFont`... | Leen recursos a partir de su id. |
| `Bitmap.resizeCrop`, `resizeFitInside`, `getRoundedCornerBitmap`, `toByteArray`, `ByteArray.toBitmap` | Transforman imágenes. |
| `EmaUriRes.getResourceId(context)`, `onDrawable`, `onString`... | Resuelven un recurso descrito por su nombre.                   |
| `Bundle.getParcelableCompat`, `Intent.getSerializableExtraCompat`... | Leen extras en todas las versiones de Android.           |
| `Intent.setInitializer`, `Activity.getInitializer`          | [Inicializadores](initializers.es.md) en los intents.              |
| `ComponentActivity.addOnBackPressedListener { }`            | Gestiona el botón atrás con un `EmaBackHandlerStrategy`.            |
| `Context.findActivity()`, `findComponentActivity()`         | Encuentran la activity que hay detrás de un contexto.               |
