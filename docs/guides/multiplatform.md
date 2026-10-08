# Kotlin Multiplatform

`ema-core` is a Kotlin Multiplatform library. All of it lives in `commonMain`, so states, actions, events,
ViewModels, use cases, `EmaResult`, `EmaText` and the rest of the core are available on every target:

| Platform        | Targets                                                                          |
|-----------------|----------------------------------------------------------------------------------|
| JVM             | `jvm` (also used by Android)                                                     |
| Web             | `js`, `wasmJs`, `wasmWasi`                                                       |
| Apple           | iOS, macOS, watchOS and tvOS, devices and simulators                             |
| Linux / Windows | `linuxX64`, `linuxArm64`, `mingwX64`                                             |
| Android Native  | `androidNativeArm32`, `androidNativeArm64`, `androidNativeX86`, `androidNativeX64` |

## Sharing the presentation layer

Put the ViewModels in the `commonMain` of a multiplatform module that depends on `ema-core`:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.github.carmabs.ema:ema-core:$emaVersion")
        }
    }
}
```

The Android app draws them with [Compose](../compose/screens.md) or [Android Views](../android-view/index.md).
On other platforms, the view subscribes to the state (`stateFlow`) and the events (`eventFlow`) of the ViewModel and
sends it actions with `dispatch`, so it can work with it whatever the platform is.

## What is not available everywhere

- **Configuration.** `EmaConfiguration.Android` is in `ema-android`. On other platforms call `Ema.init` with an
  `EmaConfiguration` built for them, or use the default one. See [Configuration](configuration.md).
- **Function names in the logs.** Only the Android configuration resolves the name of the function that launched a
  `sideEffect`. Elsewhere, pass `logName`.
- **Main dispatcher.** The default ViewModel scope uses `Dispatchers.Main`. On platforms without one (Linux, Windows,
  Android Native), configure another `mainDispatcher` or pass the scope to the ViewModel.
