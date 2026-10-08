# Fragments and Activities

`EmaFragment` and `EmaActivity` use **ViewBinding** and are generic over the binding, the state, the ViewModel and the event.

```kotlin
class CounterFragment :
    EmaFragment<CounterFragmentBinding, CounterState, CounterViewModel, CounterEvent>() {

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = CounterFragmentBinding.inflate(inflater, container, false)

    override fun provideViewModel(): CounterViewModel = CounterViewModel()

    override val navigator: EmaNavigator<CounterEvent>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bIncrement.setOnClickListener {
            viewModel.dispatch(CounterAction.Increment)
        }
    }

    override fun CounterFragmentBinding.onState(state: CounterState) {
        bindForUpdate(state::count) {
            tvCount.text = it.toString()
        }
    }

    override suspend fun CounterFragmentBinding.onEvent(event: CounterEvent) {
        when (event) {
            is CounterEvent.LimitReached ->
                Toast.makeText(requireContext(), "Limit: ${event.limit}", Toast.LENGTH_SHORT).show()
        }
    }
}
```

## What you implement

| Member                          | Fragment | Activity | Notes                                                                 |
|---------------------------------|:--------:|:--------:|-----------------------------------------------------------------------|
| `createViewBinding`             | ✔        | ✔        | Inflates the layout.                                                  |
| `provideViewModel()`            | ✔        | ✔        | Creates the ViewModel. It is called once, when the screen is opened. See [Dependency injection](../guides/dependency-injection.md). |
| `navigator`                     | ✔        | ✔        | `null` if the screen never navigates. See [Navigation](navigation.md). |
| `B.onState(state)`              | ✔        | ✔        | Renders the state.                                                    |
| `B.onEvent(event)`              | optional | optional | Handles [events](../concepts/events.md). Does nothing by default.     |
| `initializerStrategy`           | optional | ✔        | How to read the [initializer](../guides/initializers.md). `EmaFragment` and `EmaToolbarActivity` default to none. |

`onState` and `onEvent` are extension functions on the binding, so you can use the views directly
without `binding.`. `isFirstNormalExecution` tells you whether `onState` is rendering the first state of the view.

## Rendering with `bindForUpdate`

`onState` is called with every new state. Updating all the views each time is wasteful and can cause problems
(for example, resetting the cursor of a text field). `bindForUpdate` runs a block only when one field changed:

```kotlin
override fun LoginFragmentBinding.onState(state: LoginState) {
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
    if (etUser.text?.toString() != it) {
        etUser.setTextWithCursorAtEnd(it)
    }
}
```

The check is also what makes "clear the field" actions work: when the ViewModel clears the text in the state,
the field is rewritten because it no longer matches. `EmaEditText` does the check for you, see [Utilities](utilities.md).

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

### Starting the ViewModel later

To start the ViewModel only when something is ready, for example after an animation, override `startTrigger` with an
`EmaViewModelTrigger`. The lifecycle calls wait until `startViewModel()` is called, and then run in order:

```kotlin
override val startTrigger = EmaViewModelTrigger()

private fun onIntroAnimationEnd() = startTrigger.startViewModel()
```

## Sharing a ViewModel between fragments

By default a Fragment's ViewModel belongs to the Fragment. Override `fragmentViewModelScope` to attach it to the Activity:

```kotlin
override val fragmentViewModelScope = false
```

Fragments of the same Activity that use the same ViewModel class then get the same instance.

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

`EmaToolbarActivity` adds a toolbar bound to the navigation graph: the title comes from the label of the destination
and the up button goes back. It exposes `hideToolbar()`, `showToolbar()` and `setToolbarTitle()`; override
`provideFixedToolbarTitle()` to use the same title for every destination.

`overridePopTransitionAnimations()` sets the animations used when the activity closes.

## Base classes

Most apps add a `BaseFragment` that provides the dialogs and messages every screen needs.
See [Recommendations](../recommendations.md#create-base-classes).
