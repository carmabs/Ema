# Back handling

## Views (XML)

By default the back button calls `navigateBack()` of the view:

- **With a navigator**, it calls `navigator.navigateBack()`:
  `EmaFragmentNavControllerNavigator` pops the back stack and returns `false` if there was nothing to pop;
  `EmaActivityNavControllerHost` pops it and finishes the activity when the stack is empty.
- **Without a navigator** (`navigator = null`), a fragment pops its back stack or finishes the activity,
  and an activity finishes.

You do not need to do anything for the default behaviour.

### Intercepting back in a fragment

Set `handleBackPressedManually` and register your own listener. The listener returns an `EmaBackHandlerStrategy`:

| Strategy                                       | Meaning                                                     |
|------------------------------------------------|-------------------------------------------------------------|
| `EmaBackHandlerStrategy.Cancelled`             | You handled it, do nothing else.                            |
| `EmaBackHandlerStrategy.ContinueOnBackPressed` | Let the system continue the back press.                     |

```kotlin
class ManualBackFragment : EmaFragment<...>() {

    override val handleBackPressedManually = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addOnBackPressedListener {
            viewModel.dispatch(CounterAction.BackPressed)
            EmaBackHandlerStrategy.Cancelled
        }
    }
}
```

The ViewModel then decides: show a confirmation (a dialog in the state) or post an event that the view turns into
`navigateBack()`.

### Activities that own the back handling

An activity hosting a navigation graph can take over the back presses of its fragments by overriding
`ownsBackDelegate = true`. The fragments then delegate to the activity's `onBackDelegate()`, which goes back through the activity's navigator.

## Compose

Implement `onBack` in the screen content. Return the **action** to send when back is pressed, or `null` to let the
system handle it.

```kotlin
override fun onBack(state: ProfileCreationState): ProfileCreationAction? =
    if (state.overlap == null) ProfileCreationAction.OnBack else null
```

Ema installs a single `BackHandler`, enabled only when `onBack` returns an action. Returning `null` in some states is how you
turn the interception off.

- A dialog drawn with `AlertDialog` or `Dialog` handles back itself, so return `null` while it is on screen.
- It receives the **state**, so the decision can depend on it: only intercept back when there are unsaved changes.

### A complete example

The creation screen asks for confirmation before leaving. Both the toolbar arrow and the system back send the same action:

```kotlin
// Screen content
IconButton(onClick = { actions.dispatch(ProfileCreationAction.OnBack) }) { /* arrow */ }

override fun onBack(state: ProfileCreationState): ProfileCreationAction? =
    if (state.overlap == null) ProfileCreationAction.OnBack else null
```

```kotlin
// ViewModel
private fun onActionBack() = showOverlap(ProfileCreationOverlap.DialogBackConfirmation)
private fun onActionBackConfirmed() {
    hideOverlap()
    postEvent(ProfileCreationEvent.DialogConfirmationAccepted)  // the navigator pops the screen
}
```

On a screen where the arrow only goes back, send an action as well, so there is a single path through the ViewModel:

```kotlin
override fun onBack(state: ProfileOnBoardingState) = ProfileOnBoardingActions.BackClicked

// ViewModel
private fun onActionBackClicked() = postEvent(ProfileOnBoardingEvent.OnBoardingCancelled)
```
