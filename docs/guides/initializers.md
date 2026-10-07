# Initializers: passing data to a screen

An **initializer** is the data a screen needs to start. It travels in the navigation arguments (a `Bundle`)
and ends up in `onStateCreated` of the ViewModel.

## Declare it

```kotlin
@Serializable
sealed class ProfileOnBoardingInitializer : EmaInitializer {

    @Serializable
    data class Default(val admin: String) : ProfileOnBoardingInitializer()
}
```

`EmaInitializer` is an `EmaAction.Initializer`. A `sealed class` lets one screen start in different ways.
Use `EmaInitializer.EMPTY` when there is nothing to pass.

## Choose how it is serialized

| Strategy                                         | Requires                                    |
|--------------------------------------------------|---------------------------------------------|
| `BundleSerializerStrategy.kSerialization(X.serializer())` | `@Serializable` (kotlinx.serialization). **Recommended.** |
| `BundleSerializerStrategy.parcelable<X>()`       | `X : Parcelable`                            |
| `BundleSerializerStrategy.serializable<X>()`     | `X : java.io.Serializable`                  |
| `BundleSerializerStrategy.EMPTY`                 | Nothing: the screen receives no initializer |

## Send it

```kotlin
navController.navigate(
    id = R.id.action_loginFragment_to_homeFragment,
    initializerBundle = EmaInitializerBundle(
        HomeInitializer.HomeUser(user),
        BundleSerializerStrategy.kSerialization(HomeInitializer.serializer())
    )
)
```

In Compose use the `NavController.navigate(route, initializerBundle)` extension. To start an activity yourself:
`Intent(...).setInitializer(initializer, strategy)`.

## Receive it

**Fragment**: tell it how to read the bundle.

```kotlin
override val initializerStrategy: BundleSerializerStrategy
    get() = BundleSerializerStrategy.kSerialization(HomeInitializer.serializer())
```

**Activity**: the same `initializerStrategy` property (the initializer comes from the intent extras).

**Compose destination**: pass `initializerSupport` to `createComposableScreen`. `overrideInitializer` replaces the one
in the route, which is useful for the first screen of an activity, started from an intent:

```kotlin
createComposableScreen(
    initializerSupport = EmaInitializerSupport.kSerialization(
        ProfileOnBoardingInitializer.serializer(),
        getInitializer(
            BundleSerializerStrategy.kSerialization(ProfileOnBoardingInitializer.serializer()),
            savedInstanceState
        )
    ),
    ...
)
```

## Use it in the ViewModel

```kotlin
override fun onStateCreated(initializer: EmaInitializer?) {
    when (val onBoardingInitializer = initializer as ProfileOnBoardingInitializer) {
        is ProfileOnBoardingInitializer.Default -> updateState {
            copy(user = User(onBoardingInitializer.admin))
        }
    }
}
```

`onStateCreated` runs once, so a rotation does not apply the initializer twice.
