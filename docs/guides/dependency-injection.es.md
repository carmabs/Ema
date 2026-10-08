# Inyección de dependencias

Ema no depende de ningún framework de inyección de dependencias. Allí donde necesita un ViewModel, te lo pide con una
función, así que puedes usar Koin, Hilt, Kodein, un contenedor manual o constructores normales.

```kotlin
// Compose
createComposableScreen(
    screenContent = LoginScreenContent(),
    viewModel = { LoginViewModel(loginUseCase, LoginState.DEFAULT) },
    onEvent = { /* ... */ }
)

// Vistas de Android
override fun provideViewModel(): LoginViewModel = LoginViewModel(loginUseCase, LoginState.DEFAULT)
```

## Un ViewModel por pantalla

Ema llama a la función una vez, cuando se abre la pantalla por primera vez, y conserva el ViewModel mientras la pantalla
existe: consulta [Cómo se conserva el ViewModel](../concepts/viewmodel.es.md#cómo-se-conserva-el-viewmodel).
La función tiene que crear el ViewModel, no devolver uno que ya exista, para que cada pantalla tenga el suyo. Un
singleton se compartiría entre todas las pantallas de ese tipo y conservaría el estado de una pantalla que ya se cerró.

## Con Koin

Así lo hace la app `sample/`. Arranca Koin después de inicializar Ema:

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

Declara los ViewModels como `factory`, para que cada pantalla tenga el suyo. El estado inicial también se puede
inyectar, lo que mantiene el estado por defecto en un solo sitio:

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

// Vistas de Android
override fun provideViewModel(): LoginViewModel = get()
```

## Datos y casos de uso

Los repositorios suelen ser singletons y los casos de uso se crean cuando se necesitan:

```kotlin
val dataModule = module {
    single<Repository> { MockRepository() }
}

val useCaseModule = module {
    factory { LoginUseCase(get()) }
    factory { GetUserFriendsUseCase(get()) }
}
```

Los ViewModels reciben los casos de uso en el constructor, así que en los [tests](testing.es.md) pueden recibir
implementaciones falsas.
