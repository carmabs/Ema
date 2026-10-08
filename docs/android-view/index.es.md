# Vistas de Android

El módulo `ema-android-view` lleva Ema al sistema de vistas de Android: Fragments, Activities, `DialogFragment`s,
adapters de `RecyclerView` y el componente Navigation con grafos en XML.

> **Compose es la forma recomendada de construir la UI.** Este módulo se mantiene para las apps que todavía usan vistas,
> y se deprecará en el futuro en favor de [Compose](../compose/screens.es.md). Escribe las pantallas nuevas con Compose
> siempre que puedas.

```kotlin
dependencies {
    implementation("com.github.carmabs.ema:ema-android-view:$emaVersion")
}
```

Incluye `ema-android` y `ema-core`. Los ViewModels, los estados, las acciones y los eventos son exactamente los mismos
que en Compose: solo cambia la vista, así que una pantalla puede pasar de vistas a Compose sin tocar su ViewModel.

## Páginas

- [Fragments y Activities](screens.es.md): `EmaFragment`, `EmaActivity`, `EmaToolbarActivity` y `bindForUpdate`.
- [Navegación y botón atrás](navigation.es.md): navigators, inicializadores, resultados y el botón atrás.
- [Diálogos](dialogs.es.md): `EmaDialog` y los proveedores de diálogos.
- [Listas](lists.es.md): adapters y decoraciones de `RecyclerView`.
- [Permisos](permissions.es.md): el gestor de permisos de un Fragment.
- [Utilidades](utilities.es.md): extensiones de vistas, `EmaLayout` y `EmaEditText`.
