# Android Views

The `ema-android-view` module brings Ema to the Android View system: Fragments, Activities, `DialogFragment`s,
`RecyclerView` adapters and the Navigation component with XML graphs.

> **Compose is the recommended way to build the UI.** This module is kept for apps that still use Views, and it will
> be deprecated in favour of [Compose](../compose/screens.md) in the future. Write new screens with Compose when you can.

```kotlin
dependencies {
    implementation("com.github.carmabs.ema:ema-android-view:$emaVersion")
}
```

It includes `ema-android` and `ema-core`. The ViewModels, states, actions and events are exactly the same as in
Compose: only the view changes, so a screen can move from Views to Compose without touching its ViewModel.

## Pages

- [Fragments and Activities](screens.md): `EmaFragment`, `EmaActivity`, `EmaToolbarActivity` and `bindForUpdate`.
- [Navigation and back handling](navigation.md): navigators, initializers, results and the back button.
- [Dialogs](dialogs.md): `EmaDialog` and dialog providers.
- [Lists](lists.md): `RecyclerView` adapters and decorations.
- [Permissions](permissions.md): the permission manager of a Fragment.
- [Utilities](utilities.md): view extensions, `EmaLayout` and `EmaEditText`.
