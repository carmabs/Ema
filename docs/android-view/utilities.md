# View utilities

The extensions of `ema-android-view` are in `com.carmabs.ema.android.extension`.

## Views

| Helper                                          | What it does                                                       |
|-------------------------------------------------|--------------------------------------------------------------------|
| `view.afterMeasured { }`                        | Runs the block once the view has a size.                           |
| `editText.setTextWithCursorAtEnd(text)`         | Sets the text and moves the cursor to the end.                     |
| `textView.setIncrementAnimated(endValue, formatter)` | Animates a number in a text from its current value, `Int` or `Float`. |
| `imageView.setImageDrawableWithTransition(drawable)` | Cross fades from the current image to the new one.            |
| `imageView.setImageWithOvershot(drawable)`      | Sets the image with an overshoot rotation.                         |
| `activity.hideKeyboard()`, `fragment.hideKeyboard()`, `view.hideKeyboard()` | Hides the soft keyboard.             |
| `progressBar.setProgressTintCompat(color)`      | Tints a progress bar.                                              |
| `checkVisibility(visible, gone)`                | `VISIBLE`, `GONE` or `INVISIBLE` from a boolean.                   |
| `checkUpdate(old, new) { }`                     | Runs the block only if the value changed.                          |
| `getColorFromGradient(colors, positions, value)`, `lerpColor(a, b, t)` | Interpolate colours.                        |

## Animations

`animator.pauseAll()`, `resumeAll()`, `endAll()`, `cancelAll()` and `isSomeRunning()` act on an `AnimatorSet` and all its
children.

## Fragments

| Helper                                          | What it does                                                       |
|-------------------------------------------------|--------------------------------------------------------------------|
| `fragment.setInitializer(initializer, strategy)` / `getInitializer(strategy)` | The [initializer](../guides/initializers.md) in the arguments. |
| `fragment.addOnBackPressedListener { }`         | Handles the back button. See [Back handling](navigation.md#back-handling). |
| `fragment.setEmaResultListener<T> { }`          | Receives the result of the next fragment. See [Results](navigation.md#results). |
| `navController.navigate(id, initializerBundle)` | Navigates with an initializer.                                     |

## `EmaLayout`

A base class for custom views with their own data, written like a small screen:

```kotlin
class UserCardLayout(context: Context, attrs: AttributeSet) :
    EmaLayout<LayoutUserCardBinding, User>(context, attrs) {

    override fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        LayoutUserCardBinding.inflate(inflater, container, false)

    override fun createInitialState() = User.EMPTY

    override fun LayoutUserCardBinding.setup(data: User) {
        tvName.text = data.fullName
    }

    override fun getAttributes(): IntArray? = null

    override fun setupAttributes(ta: TypedArray) = Unit
}
```

`updateData { copy(...) }` changes the data and redraws the view. `getAttributes` and `setupAttributes` read the custom
XML attributes of the view.

## `EmaEditText`

An `EditText` whose `setText(String)` only writes the text when it is different, so the cursor does not jump while the
user types. `setEmaTextWatcherListener { text -> }` notifies the changes made by the user, not the ones made by
`setText` inside the listener itself. `EmaTextWatcher` adds the same listener to any `EditText`.

## `emaStateDelegate`

A property delegate that creates its initial value lazily. `EmaDialog` and `EmaLayout` use it for their data.
