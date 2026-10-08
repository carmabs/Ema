# Initializers: passing data to a screen

An **initializer** is the data a screen needs to start. It travels in the navigation arguments (a `Bundle`)
and ends up in `onStateCreated` of the ViewModel.

## Declare it

```kotlin
@Serializable
sealed class ProfileOnBoardingInitializer : EmaAction.Initializer {

    @Serializable
    data class Default(val admin: String) : ProfileOnBoardingInitializer()
}
```

A `sealed` type lets one screen start in different ways. Use `EmaAction.Initializer.EMPTY` when there is nothing to pass.

## Choose how it is serialized

| Strategy                                                  | Requires                                     | Compose routes |
|-----------------------------------------------------------|----------------------------------------------|:--------------:|
| `BundleSerializerStrategy.kSerialization(X.serializer())` | `@Serializable` (kotlinx.serialization). **Recommended.** | ✔ |
| `BundleSerializerStrategy.parcelable<X>()`                | `X : Parcelable`                             |                |
| `BundleSerializerStrategy.serializable<X>()`              | `X : java.io.Serializable`                   |                |
| `BundleSerializerStrategy.EMPTY`                          | Nothing: the screen receives no initializer  | ✔              |

In Compose the initializer travels as text inside the route, and only `kSerialization` can turn it back into an object.
Any character is allowed in its values: the route encodes them.

## Send and receive it

How the initializer travels depends on the UI technology:

- **Compose**: `NavController.navigate(route, initializerBundle)` and the `initializerSupport` of the destination.
  See [Compose navigation](../compose/navigation.md#passing-an-initializer).
- **Android Views**: `NavController.navigate(id, initializerBundle)` and the `initializerStrategy` of the Fragment or
  Activity. See [Android Views navigation](../android-view/navigation.md#passing-an-initializer).

To start an activity yourself, put it in the intent with `Intent(...).setInitializer(initializer, strategy)`, and read it
in the activity with `getInitializer(strategy, savedInstanceState)`. These extensions are in `ema-android`.

## Use it in the ViewModel

```kotlin
override fun onStateCreated(initializer: EmaAction.Initializer?) {
    when (val onBoardingInitializer = initializer as ProfileOnBoardingInitializer) {
        is ProfileOnBoardingInitializer.Default -> updateState {
            copy(user = User(onBoardingInitializer.admin))
        }
    }
}
```

`onStateCreated` runs once, so a rotation does not apply the initializer twice.
