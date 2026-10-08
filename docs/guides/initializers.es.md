# Inicializadores: pasar datos a una pantalla

Un **inicializador** son los datos que necesita una pantalla para empezar. Viaja en los argumentos de navegación (un
`Bundle`) y llega a `onStateCreated` del ViewModel.

## Declararlo

```kotlin
@Serializable
sealed class ProfileOnBoardingInitializer : EmaAction.Initializer {

    @Serializable
    data class Default(val admin: String) : ProfileOnBoardingInitializer()
}
```

Un tipo `sealed` permite que una pantalla empiece de formas distintas. Usa `EmaAction.Initializer.EMPTY` cuando no hay
nada que pasar.

## Elegir cómo se serializa

| Estrategia                                                | Requiere                                     | Rutas de Compose |
|-----------------------------------------------------------|----------------------------------------------|:----------------:|
| `BundleSerializerStrategy.kSerialization(X.serializer())` | `@Serializable` (kotlinx.serialization). **Recomendada.** | ✔ |
| `BundleSerializerStrategy.parcelable<X>()`                | `X : Parcelable`                             |                  |
| `BundleSerializerStrategy.serializable<X>()`              | `X : java.io.Serializable`                   |                  |
| `BundleSerializerStrategy.EMPTY`                          | Nada: la pantalla no recibe inicializador    | ✔                |

En Compose el inicializador viaja como texto dentro de la ruta, y solo `kSerialization` puede convertirlo de nuevo en un
objeto. Sus valores admiten cualquier carácter: la ruta los codifica.

## Enviarlo y recibirlo

Cómo viaja el inicializador depende de la tecnología de UI:

- **Compose**: `NavController.navigate(route, initializerBundle)` y el `initializerSupport` del destino.
  Consulta [Navegación en Compose](../compose/navigation.es.md#pasar-un-inicializador).
- **Vistas de Android**: `NavController.navigate(id, initializerBundle)` y el `initializerStrategy` del Fragment o la
  Activity. Consulta [Navegación con vistas de Android](../android-view/navigation.es.md#pasar-un-inicializador).

Para abrir una activity tú mismo, mételo en el intent con `Intent(...).setInitializer(initializer, strategy)` y léelo en
la activity con `getInitializer(strategy, savedInstanceState)`. Estas extensiones están en `ema-android`.

## Usarlo en el ViewModel

```kotlin
override fun onStateCreated(initializer: EmaAction.Initializer?) {
    when (val onBoardingInitializer = initializer as ProfileOnBoardingInitializer) {
        is ProfileOnBoardingInitializer.Default -> updateState {
            copy(user = User(onBoardingInitializer.admin))
        }
    }
}
```

`onStateCreated` se ejecuta una vez, así que una rotación no aplica el inicializador dos veces.
