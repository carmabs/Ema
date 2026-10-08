# Kotlin Multiplatform

`ema-core` es una librería Kotlin Multiplatform. Todo su código está en `commonMain`, así que los estados, las acciones,
los eventos, los ViewModels, los casos de uso, `EmaResult`, `EmaText` y el resto del núcleo están disponibles en todos
los targets:

| Plataforma      | Targets                                                                          |
|-----------------|----------------------------------------------------------------------------------|
| JVM             | `jvm` (el que usa también Android)                                               |
| Web             | `js`, `wasmJs`, `wasmWasi`                                                       |
| Apple           | iOS, macOS, watchOS y tvOS, dispositivos y simuladores                           |
| Linux / Windows | `linuxX64`, `linuxArm64`, `mingwX64`                                             |
| Android Native  | `androidNativeArm32`, `androidNativeArm64`, `androidNativeX86`, `androidNativeX64` |

## Compartir la capa de presentación

Pon los ViewModels en el `commonMain` de un módulo multiplataforma que dependa de `ema-core`:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.github.carmabs.ema:ema-core:$emaVersion")
        }
    }
}
```

## Lo que no está disponible en todas partes

- **Configuración.** `EmaConfiguration.Android` está en `ema-android`. En otras plataformas llama a `Ema.init` con una
  `EmaConfiguration` hecha para ellas, o usa la configuración por defecto. Consulta [Configuración](configuration.es.md).
- **Nombres de función en los logs.** Solo la configuración de Android obtiene el nombre de la función que lanzó un
  `sideEffect`. En el resto, pasa `logName`.
- **Dispatcher principal.** El scope por defecto de los ViewModels usa `Dispatchers.Main`. En las plataformas que no lo
  tienen (Linux, Windows, Android Native), configura otro `mainDispatcher` o pasa el scope al ViewModel.
