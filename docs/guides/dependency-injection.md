# Dependency injection

Ema uses [Koin](https://insert-koin.io/). `EmaApplication` (or `initializeEma` from `EmaApplicationAware`) starts it for you
and registers its own module; you only list yours:

```kotlin
override fun KoinApplication.injectAppModules(): List<Module> =
    listOf(dataModule, useCaseModule, uiModule)
```

## Declare ViewModels as `factory`

```kotlin
val uiModule = module {
    factory { SplashViewModel() }
    factoryOf(::LoginViewModel)
    factoryOf(::HomeViewModel)

    factory { LoginState.DEFAULT }
    factory { HomeState.DEFAULT }
}
```

It must be a `factory`, not a `single`. Ema keeps the real instance alive in an Android `ViewModel`;
Koin only creates the "seed" that is used the first time a screen is opened.
See [ViewModel](../concepts/viewmodel.md#how-the-viewmodel-is-kept-alive).

The initial state can be injected too (`initialDataState: LoginState` in the constructor), which keeps the default state
in one place.

## Getting the ViewModel in a view

Fragments and activities resolve it from their own Koin scope:

```kotlin
override fun provideViewModel(): LoginViewModel = injectDirect()
```

Outside of them (Compose, plain activities) use the global overload:

```kotlin
createComposableScreen(
    viewModel = { injectDirect<ProfileCreationViewModel>() },
    ...
)
```

In a composable, `injectDirectRemembered<T>()` resolves it once and remembers it.

## Passing parameters

Definitions can receive parameters, for example a `FragmentManager` for dialogs:

```kotlin
factory { (fragmentManager: FragmentManager) ->
    AppDialogProvider(
        fragmentManager,
        SimpleDialogProvider(fragmentManager),
        LoadingDialogProvider(fragmentManager),
        ErrorDialogProvider(fragmentManager)
    )
}
```

```kotlin
private val appDialogProvider: AppDialogProvider by inject {
    parametersOf(childFragmentManager)
}
```

## Data and use cases

Declare repositories as `single` and use cases as `factory`:

```kotlin
val dataModule = module {
    single<Repository> { MockRepository() }
}

val useCaseModule = module {
    factory { LoginUseCase(get()) }
    factory { GetUserFriendsUseCase(get()) }
}
```
