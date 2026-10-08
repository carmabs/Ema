# Compose utilities

The helpers of `ema-compose` are in `com.carmabs.ema.compose.extension`.

## Texts and resources

```kotlin
state.title.stringResource()            // EmaText → String
R.string.welcome.toComposeString(name)  // a string resource with arguments
R.drawable.logo.toComposePainter()      // Painter
R.drawable.ic_add.toComposeImageVector()// ImageVector, from a vector drawable
R.color.primary.toComposeColor()        // Color
```

`EmaImage` is drawn with `toComposableImage(contentDescription, modifier, ...)`, or turned into a `Painter` with
`toComposePainter()`. It accepts the three kinds of image: resource `Id`, `Uri` and `ByteArray`.

## Previews

| Helper                                        | What it does                                                    |
|-----------------------------------------------|-----------------------------------------------------------------|
| `isInPreview()`                               | `true` inside the IDE preview.                                  |
| `skipForPreview(previewComposable) { }`       | Draws the content, or `previewComposable` inside a preview.     |
| `value.changeForPreview(previewValue)`        | Returns `previewValue` inside a preview, `value` otherwise.     |
| `EmaImmutableActionDispatcher.EMPTY`          | An action dispatcher that ignores every action.                 |
| `LoremIpsum(words).generate()`                | Placeholder text as a single `String`.                          |

## Sizes

| Helper                                          | What it does                                     |
|-------------------------------------------------|--------------------------------------------------|
| `screenWidthDp()`, `screenHeightDp()`           | The screen size in `Dp`.                         |
| `screenWidthPx()`, `screenHeightPx()`           | The screen size in pixels.                       |
| `10.pxToDp()`, `10.dp.toPx()`, `10.dp.inSp`     | Unit conversions with the current density.       |
| `EmaMeasureViewWidth(viewToMeasure) { width, view -> }` | Measures a composable before drawing the content that depends on its width. |

## Modifiers and animations

- `Modifier.fade(fadeSide, startFadePercentage)` fades the content out towards one side: `FadeSide.TOP`, `BOTTOM`,
  `START` or `END`. Useful for scrollable content.
- Inside `AnimatedVisibility`, `setOnBeforeVisibleListener { }`, `setOnVisibleListener { }` and `setOnHideListener { }`
  run code when the enter or exit animation reaches each state.

## Others

| Helper                                          | What it does                                                     |
|-------------------------------------------------|------------------------------------------------------------------|
| `LocalContext.activity`                         | The `ComponentActivity` that contains the composable.            |
| `list.toImmutable()`                            | `EmaImmutableList`, a list Compose treats as stable.             |
| `viewModel.asActionDispatcher<A>()`             | The ViewModel as an `EmaActionDispatcher<A>`.                    |
| `viewModel.asViewModelAction<S, A, E>()`        | The ViewModel as an `EmaViewModelAction<S, A, E>`.               |
| `ScreenContent::class.routeId`                  | The route of a screen. See [Navigation](navigation.md#routes).   |
