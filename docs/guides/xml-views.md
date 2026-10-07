# Views with XML

Ema supports classic Views through `EmaFragment` and `EmaActivity`. Both use **ViewBinding** and are generic over
the binding, the state, the ViewModel and the event.

```kotlin
class LoginFragment :
    BaseFragment<LoginFragmentBinding, LoginState, LoginViewModel, LoginEvent>() {

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = LoginFragmentBinding.inflate(inflater, container, false)

    override fun provideViewModel(): LoginViewModel = injectDirect()

    override val navigator: EmaNavigator<LoginEvent> = LoginNavigator(this)

    override fun LoginFragmentBinding.onState(state: LoginState) { /* render */ }

    override suspend fun LoginFragmentBinding.onEvent(event: LoginEvent) { /* react */ }
}
```

## What you implement

| Member                          | Fragment | Activity | Notes                                                                 |
|---------------------------------|:--------:|:--------:|-----------------------------------------------------------------------|
| `createViewBinding`             | ✔        | ✔        | Inflates the layout.                                                  |
| `provideViewModel()`            | ✔        | ✔        | Return `injectDirect()`, see [Dependency injection](dependency-injection.md). |
| `navigator`                     | ✔        | ✔        | `null` if the screen never navigates.                                 |
| `B.onState(state)`              | ✔        | ✔        | Renders the state.                                                    |
| `B.onEvent(event)`              | optional | optional | Handles [events](../concepts/events.md). Does nothing by default.     |
| `initializerStrategy`           | optional | ✔        | Needed only to receive an [initializer](initializers.md). Fragments default to none; `EmaToolbarActivity` does too. |

`onState` and `onEvent` are extension functions on the binding, so you can use the views directly
without `binding.`.

## Rendering with `bindForUpdate`

`onState` is called with every new state. Updating all the views each time is wasteful and can cause problems
(for example, resetting the cursor of a text field). `bindForUpdate` runs a block only when one field changed:

```kotlin
override fun LoginFragmentBinding.onState(state: LoginState) {
    bindForUpdate(state::userName) {
        etUser.setTextWithCursorAtEnd(it)
    }
    bindForUpdate(state::userNameError) {
        tilLoginUser.error = if (it) getString(R.string.login_error_user_empty) else null
    }
    bindForUpdate(state::isLoading) {
        pbLoginSign.isVisible = it
    }
}
```

- The first time a view is rendered, every block runs. After that, a block runs only if its field changed.
- After the view is recreated (rotation, coming back from the back stack) everything runs again.
- The argument must be a property reference of the **same `state` parameter**: `state::userName`.
- For objects with a custom notion of equality, pass `areEqualComparator`.
- `bindForUpdateWithPrevious` also gives you the old value.

> `bindForUpdate` finds the previous value by reflecting over the field name. If you enable R8 or ProGuard,
> keep the fields of your state classes (for example `-keepclassmembers class * implements com.carmabs.ema.core.state.EmaState { *; }`).

## Sending actions

Call `viewModel.dispatch(...)`:

```kotlin
bLoginSign.setOnClickListener { viewModel.dispatch(LoginAction.Login) }
```

## Text fields

Keep the text in the state, so it survives a rotation. Every keystroke becomes an action, the ViewModel stores
the text, and the new state comes back to `onState`:

```kotlin
etUser.addTextChangedListener {
    viewModel.dispatch(LoginAction.UserNameWritten(it?.toString() ?: STRING_EMPTY))
}
```

When the state comes back, write it into the field **only if it differs**. Writing the same text would move the
cursor to the end while the user is editing in the middle of the text:

```kotlin
bindForUpdate(state::userName) {
    if (etUser.text?.toString() != it)
        etUser.setTextWithCursorAtEnd(it)
}
```

The check is also what makes `DeleteUser`-style actions work: when the ViewModel clears the text in the state,
the field is rewritten because it no longer matches.

## Lifecycle

| View callback  | What Ema does                                                                   |
|----------------|---------------------------------------------------------------------------------|
| `onViewCreated` (Fragment) / `onCreate` (Activity) | `viewModel.onCreated(initializer)`                    |
| `onStart`      | `viewModel.onStartView()`                                                       |
| `onResume`     | Starts collecting state and events (once), then `viewModel.onResumeView()`.     |
| `onPause`      | `viewModel.onPauseView()`                                                       |
| `onStop`       | Stops collecting, then `viewModel.onStopView()`.                                |

The state is collected in `onResume` and not earlier because dialogs and fragments cannot be shown safely
before the saved state has been restored.

## Activities

An Activity that only hosts fragments uses `EmaViewModel.EMPTY` and a navigator that owns the navigation graph:

```kotlin
class SplashActivity :
    EmaToolbarActivity<SplashActivityBinding, EmaState.EMPTY, EmaViewModel.EMPTY, EmaEvent.EMPTY>() {

    override fun createViewBinding(inflater: LayoutInflater) = SplashActivityBinding.inflate(inflater)

    override fun provideViewModel() = EmaViewModel.EMPTY

    override fun SplashActivityBinding.onState(data: EmaState.EMPTY) = Unit

    override val navigator: EmaNavigator<EmaEvent.EMPTY> = EmaActivityNavControllerHost(
        this,
        R.id.navHostFragment,
        R.navigation.main_graph
    )

    override fun SplashActivityBinding.provideToolbar() = tbSplash
    override fun SplashActivityBinding.provideToolbarLayout() = ablSplash
}
```

`EmaToolbarActivity` adds a toolbar bound to the navigation graph (title from the destination label, up button).
It exposes `hideToolbar()`, `showToolbar()` and `setToolbarTitle()`.

## Base classes

Most apps add a `BaseFragment` that provides the dialogs and messages every screen needs.
See [Recommendations](../recommendations.md#create-base-classes).

## Hosting Compose in a Fragment or Activity

`EmaComposableFragment` and `EmaComposableActivity` are the same as the classes above but draw a Composable.
See [Compose](compose.md#hosting-compose-in-a-fragment-or-activity).
