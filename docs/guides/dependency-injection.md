# Dependency injection

Ema does not depend on any dependency injection framework. Wherever it needs a ViewModel, it asks you for it with a
function, so you can use Koin, Hilt, Kodein, a manual container or plain constructors.

```kotlin
// Compose
createComposableScreen(
    screenContent = LoginScreenContent(),
    viewModel = { LoginViewModel(loginUseCase, LoginState.DEFAULT) },
    onEvent = { /* ... */ }
)

// Android Views
override fun provideViewModel(): LoginViewModel = LoginViewModel(loginUseCase, LoginState.DEFAULT)
```

## One ViewModel per screen

Ema calls the function once, when the screen is opened for the first time, and keeps the ViewModel while the screen
exists: see [How the ViewModel is kept alive](../concepts/viewmodel.md#how-the-viewmodel-is-kept-alive).
The function must create the ViewModel, not return one that already exists, so each screen gets its own. A singleton
would be shared by every screen of that type and would keep the state of a screen that was already closed.

## With Koin

This is how the `sample/` app does it. Start Koin after initializing Ema:

```kotlin
class EmaSampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Ema.init(EmaConfiguration.Android)
        startKoin {
            androidContext(this@EmaSampleApplication)
            modules(dataModule, useCaseModule, uiModule)
        }
    }
}
```

Declare the ViewModels as `factory`, so each screen gets its own. The initial state can be injected too, which
keeps the default state in one place:

```kotlin
val uiModule = module {
    factoryOf(::LoginViewModel)
    factoryOf(::HomeViewModel)

    factory { LoginState.DEFAULT }
    factory { HomeState.DEFAULT }
}
```

```kotlin
// Compose
createComposableScreen(
    screenContent = ProfileCreationScreenContent(),
    viewModel = { get<ProfileCreationViewModel>() },
    onEvent = { /* ... */ }
)

// Android Views
override fun provideViewModel(): LoginViewModel = get()
```

## Data and use cases

Repositories are usually singletons and use cases are created when needed:

```kotlin
val dataModule = module {
    single<Repository> { MockRepository() }
}

val useCaseModule = module {
    factory { LoginUseCase(get()) }
    factory { GetUserFriendsUseCase(get()) }
}
```

The ViewModels receive the use cases in the constructor, so they can receive fakes in the [tests](testing.md).
