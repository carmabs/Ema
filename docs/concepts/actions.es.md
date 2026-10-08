# Acciones

Una acción es **algo que ha hecho el usuario**: "ha pulsado login", "ha escrito un nombre", "ha confirmado el diálogo".
Las acciones van de la vista al ViewModel.

```kotlin
sealed interface LoginAction : EmaAction {

    data object Login : LoginAction
    data object DeleteUser : LoginAction
    data class UserNameWritten(val user: String) : LoginAction
    data class PasswordWritten(val password: String) : LoginAction

    sealed interface Error : LoginAction {
        data object BadCredentialsAccepted : Error
        data object BackPressed : Error
    }
}
```

## Nombres

Pon a las acciones el nombre de **lo que ha hecho el usuario**, en pasado, no de lo que debería hacer el ViewModel.
`UserNameWritten` es mejor que `UpdateUserName`: la vista informa del hecho y el ViewModel decide qué hacer con él.

Usa una `sealed interface` para que el `when` sea exhaustivo. Al añadir una acción, el compilador te indica todos los
sitios donde tienes que gestionarla.

## Gestionar las acciones

Hereda de `EmaViewModelAction<State, Action, Event>` e implementa `onAction`:

```kotlin
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    initialDataState: LoginState
) : EmaViewModelAction<LoginState, LoginAction, LoginEvent>(initialDataState) {

    override fun onAction(action: LoginAction) {
        when (action) {
            LoginAction.Login -> onActionLogin()
            LoginAction.DeleteUser -> onActionDeleteUser()
            is LoginAction.UserNameWritten -> onActionUserWrite(action.user)
            is LoginAction.PasswordWritten -> onActionPasswordWrite(action.password)
            LoginAction.Error.BadCredentialsAccepted -> hideOverlap()
            LoginAction.Error.BackPressed -> hideOverlap()
        }
    }
}
```

`onAction` es `protected`. El punto de entrada público es `dispatch(action)`, así que la vista solo puede comunicarse
mediante acciones y no puede llamar a funciones arbitrarias del ViewModel.

Si prefieres no declarar acciones, `EmaViewModelBasic<State, Event>` permite que la vista llame a funciones públicas del
ViewModel, como en el MVVM clásico. Se recomiendan las acciones: consulta [ViewModel](viewmodel.es.md).

## Enviarlas desde la vista

El contenido de una pantalla Compose recibe un `EmaImmutableActionDispatcher`, un envoltorio estable que no provoca
recomposiciones innecesarias:

```kotlin
Button(onClick = { actions.dispatch(LoginAction.Login) }) { ... }
```

En las previews usa `EmaImmutableActionDispatcher.EMPTY`, que ignora todas las acciones. Fuera de Compose,
`EmaActionDispatcher.EMPTY` hace lo mismo. Con las vistas de Android, las acciones se envían con `viewModel.dispatch(...)`,
consulta [Vistas de Android](../android-view/screens.es.md#enviar-acciones).

## Tipos de acción especiales

| Tipo                      | Uso                                                               |
|---------------------------|-------------------------------------------------------------------|
| `EmaAction`               | Las acciones de una pantalla. Tus acciones heredan de este tipo.  |
| `EmaAction.Initializer`   | Los datos con los que empieza una pantalla. Consulta [Inicializadores](../guides/initializers.es.md). |
| `EmaAction.EMPTY`         | El tipo de acción de una pantalla sin acciones.                   |
