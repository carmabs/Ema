# Permisos

`EmaPermissionManager` envuelve el flujo de permisos de Android en funciones `suspend` y en un pequeño modelo de
peticiones que encaja con el flujo estado/ViewModel. El resultado de un permiso es uno de estos tres valores:

| `PermissionState`             | Significado                                                          |
|-------------------------------|----------------------------------------------------------------------|
| `GRANTED`                     | El permiso está concedido.                                           |
| `NOT_GRANTED`                 | No está concedido.                                                   |
| `NOT_GRANTED_SHOULD_EXPLAIN`  | No está concedido, y Android recomienda explicar por qué se pide.    |

Declara los permisos en el manifest como siempre.

## El gestor

La vista crea el gestor:

- Compose: `rememberEmaPermissionManager()`. Consulta [Permisos en Compose](../compose/permissions.es.md).
- Fragments: `EmaAndroidPermissionManager(fragment)`. Consulta [Permisos con vistas de Android](../android-view/permissions.es.md).

```kotlin
permissionManager.isPermissionGranted(Manifest.permission.CAMERA)   // PermissionState
permissionManager.areAllPermissionsGranted(a, b)                    // Boolean
permissionManager.shouldShowRequestPermissionRationale(permission)  // Boolean
permissionManager.requestPermission(permission)                     // suspend → PermissionState
permissionManager.requestMultiplePermission(a, b)                   // suspend → Map<String, PermissionState>
```

Haz las peticiones de una en una. Android cancela una petición hecha mientras otra está en pantalla: devuelve
`NOT_GRANTED` (un mapa vacío si son varios permisos) en lugar de esperar.

### Ubicación

Hay atajos para la ubicación: `requestCoarseLocationPermission()`, `requestFineLocationPermission()`,
`isLocationCoarseGranted()`, `isLocationFineGranted()` e `isLocationBackgroundGranted()`.

Pide la ubicación precisa con `requestFineLocationPermission()`, no con `requestPermission`: según la versión de Android,
hay que pedirla junto con la aproximada. `requestBackgroundLocationPermission(infoDialogType)` de
`EmaAndroidPermissionManager` pide la ubicación en segundo plano, que necesita antes la precisa. En las versiones
recientes de Android el usuario la concede en los ajustes del sistema, e `infoDialogType` decide la explicación que se
muestra antes de abrirlos: `InfoDialogType.Default(title, message)`, o
`InfoDialogType.CustomDialog(alertDialog, onAcceptClickListener)` para mostrar tu propio diálogo.

## Pedir un permiso desde el ViewModel

El ViewModel no toca Android, así que **mete la petición en el estado**. La vista la ejecuta.

```kotlin
data class LocationState(
    val permissionRequest: EmaPermissionRequest = EmaPermissionRequest.cancelRequest()
) : EmaState
```

```kotlin
// ViewModel
private fun onActionAllowClicked() {
    updateState {
        copy(permissionRequest = EmaPermissionRequest.createRequest { permissionState ->
            onPermissionResponse(permissionState)
        })
    }
}

private fun onPermissionResponse(permissionState: PermissionState) {
    updateState { copy(permissionRequest = EmaPermissionRequest.cancelRequest()) }  // borra la petición
    when (permissionState) {
        PermissionState.GRANTED -> { /* ... */ }
        PermissionState.NOT_GRANTED -> { /* ... */ }
        PermissionState.NOT_GRANTED_SHOULD_EXPLAIN -> { /* ... */ }
    }
}
```

La vista llama a `permissionManager.handleRequest(request, scope, permission)` cada vez que cambia la petición del estado.
`EmaPermissionRequest.cancelRequest()` es la petición de "no hay nada que hacer", que `handleRequest` ignora.
`EmaMultiplePermissionRequest` y `handleRequestMultiple` funcionan igual con varios permisos.

Si el proceso muere mientras el usuario está en los ajustes del sistema, restaura la pantalla con
[un save state manager](../compose/save-state.es.md).
