# Permisos en Compose

Obtén el [gestor de permisos](../guides/permissions.es.md) con `rememberEmaPermissionManager()`. En una preview devuelve
un gestor que lo concede todo.

```kotlin
@Composable
override fun onState(state: LocationState, actions: EmaImmutableActionDispatcher<LocationAction>) {
    val permissionManager = rememberEmaPermissionManager()
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.permissionRequest) {
        permissionManager.handleRequest(state.permissionRequest, scope, Manifest.permission.ACCESS_COARSE_LOCATION)
    }
}
```

El ViewModel mete un `EmaPermissionRequest` en el estado, y la pantalla lo ejecuta cada vez que cambia.
Consulta [Pedir un permiso desde el ViewModel](../guides/permissions.es.md#pedir-un-permiso-desde-el-viewmodel).

Las funciones suspendidas del gestor también se pueden llamar desde una corrutina de la pantalla:

```kotlin
scope.launch {
    val cameraState = permissionManager.requestPermission(Manifest.permission.CAMERA)
}
```
