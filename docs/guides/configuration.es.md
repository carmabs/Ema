# Configuración

Ema se configura una vez, cuando arranca la aplicación, con `Ema.init`:

```kotlin
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Ema.init(EmaConfiguration.Android)
    }
}
```

- Llámalo **antes de crear ningún ViewModel**: los ViewModels leen la configuración al construirse.
- Solo se puede llamar **una vez**. Una segunda llamada lanza `IllegalStateException`.
- Si no se llama nunca, se usa la configuración por defecto, `EmaConfiguration()`. Es lo habitual en los tests unitarios
  y en las plataformas que no son Android.

## Qué se puede configurar

| Propiedad                     | `EmaConfiguration()` por defecto | `EmaConfiguration.Android`     | Para qué                                                  |
|-------------------------------|------------------------------|------------------------------------|-----------------------------------------------------------|
| `mainDispatcher`              | `Dispatchers.Main`           | `Dispatchers.Main.immediate`       | Dónde ejecutan los ViewModels sus corrutinas por defecto. |
| `useCaseBackgroundDispatcher` | `Dispatchers.Default`        | `Dispatchers.IO`                   | Dónde se ejecutan por defecto los [casos de uso](utilities.es.md#casos-de-uso). |
| `sideEffectConfig`            | `EmaSideEffectConfig()`      | Obtiene el nombre de los métodos de las lambdas | Cómo gestiona `sideEffect` los errores y qué informa. Consulta [Trabajo asíncrono y errores](async-and-errors.es.md#errores). |
| `dataClassPrinter`            | `EmaDataClassPrinter.ToString` | Imprime todos los campos, con sangría | Cómo imprime los objetos `toStringPretty()`.          |

`EmaConfiguration` es una data class, así que personaliza la configuración de Android con `copy`:

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

`EmaConfiguration.Android` está en `ema-android` (`com.carmabs.ema.android.configuration.Android`).

## Nombrar los side effects

Cada `sideEffect` informa del nombre de la función que lo lanzó, algo útil para registrar los errores.

- `EmaConfiguration.Android` lo obtiene de la clase que el compilador genera para la lambda, así que un `sideEffect` en
  `doLogin()` aparece como `doLogin`.
- En otras plataformas el nombre es genérico. Pasa `logName` para ponerlo tú, en cualquier plataforma:

```kotlin
sideEffect(logName = "login") { loginUseCase(input) }
```

Para decidir el nombre de otra forma, implementa `EmaMethodNameResolver` y asígnalo en `EmaSideEffectConfig.methodNameResolver`.

## Imprimir objetos

`toStringPretty()` imprime cualquier objeto con el `EmaDataClassPrinter` configurado. `EmaConfiguration.Android` usa
`EmaAndroidDataClassPrinter`, que imprime los campos, los objetos anidados, las colecciones y los mapas con sangría.
Implementa `EmaDataClassPrinter` para imprimirlos a tu manera.

## El broadcast manager

`Ema.broadcastManager` es el `EmaBroadcastManager` global de la app. Consulta
[Resultados entre pantallas](results-between-screens.es.md#broadcasts-globales).
