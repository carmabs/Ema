# Utilidades de las vistas

Las extensiones de `ema-android-view` están en `com.carmabs.ema.android.extension`.

## Vistas

| Ayuda                                           | Qué hace                                                           |
|-------------------------------------------------|--------------------------------------------------------------------|
| `view.afterMeasured { }`                        | Ejecuta el bloque cuando la vista ya tiene tamaño.                 |
| `editText.setTextWithCursorAtEnd(text)`         | Asigna el texto y lleva el cursor al final.                        |
| `textView.setIncrementAnimated(endValue, formatter)` | Anima un número de un texto desde su valor actual, `Int` o `Float`. |
| `imageView.setImageDrawableWithTransition(drawable)` | Hace un fundido de la imagen actual a la nueva.               |
| `imageView.setImageWithOvershot(drawable)`      | Asigna la imagen con una rotación con rebote.                      |
| `activity.hideKeyboard()`, `fragment.hideKeyboard()`, `view.hideKeyboard()` | Oculta el teclado.                     |
| `progressBar.setProgressTintCompat(color)`      | Tiñe una barra de progreso.                                        |
| `checkVisibility(visible, gone)`                | `VISIBLE`, `GONE` o `INVISIBLE` a partir de un booleano.           |
| `checkUpdate(old, new) { }`                     | Ejecuta el bloque solo si el valor ha cambiado.                    |
| `getColorFromGradient(colors, positions, value)`, `lerpColor(a, b, t)` | Interpolan colores.                         |

## Animaciones

`animator.pauseAll()`, `resumeAll()`, `endAll()`, `cancelAll()` e `isSomeRunning()` actúan sobre un `AnimatorSet` y
todos sus hijos.

## Fragments

| Ayuda                                           | Qué hace                                                           |
|-------------------------------------------------|--------------------------------------------------------------------|
| `fragment.setInitializer(initializer, strategy)` / `getInitializer(strategy)` | El [inicializador](../guides/initializers.es.md) en los argumentos. |
| `fragment.addOnBackPressedListener { }`         | Gestiona el botón atrás. Consulta [Botón atrás](navigation.es.md#botón-atrás). |
| `fragment.setEmaResultListener<T> { }`          | Recibe el resultado del fragment siguiente. Consulta [Resultados](navigation.es.md#resultados). |
| `navController.navigate(id, initializerBundle)` | Navega con un inicializador.                                       |

## `EmaLayout`

Una clase base para vistas personalizadas con sus propios datos, escrita como una pequeña pantalla:

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

`updateData { copy(...) }` cambia los datos y repinta la vista. `getAttributes` y `setupAttributes` leen los atributos
XML propios de la vista.

## `EmaEditText`

Un `EditText` cuyo `setText(String)` solo escribe el texto cuando es distinto, para que el cursor no salte mientras el
usuario escribe. `setEmaTextWatcherListener { text -> }` avisa de los cambios que hace el usuario, no de los que hace
`setText` dentro del propio listener. `EmaTextWatcher` añade el mismo listener a cualquier `EditText`.

## `emaStateDelegate`

Un delegado de propiedad que crea su valor inicial de forma perezosa. `EmaDialog` y `EmaLayout` lo usan para sus datos.
