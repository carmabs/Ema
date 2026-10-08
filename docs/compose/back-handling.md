# Compose back handling

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

## A complete example

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

The navigator turns the event into `navigateBack()`. See [Navigation](navigation.md).
