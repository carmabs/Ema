# Permissions

`EmaPermissionManager` wraps the Android permission flow in `suspend` functions and a small request model that fits
the state/ViewModel flow. The permission result is one of three values:

| `PermissionState`             | Meaning                                                              |
|-------------------------------|----------------------------------------------------------------------|
| `GRANTED`                     | The permission is granted.                                           |
| `NOT_GRANTED`                 | Not granted.                                                         |
| `NOT_GRANTED_SHOULD_EXPLAIN`  | Not granted, and Android recommends showing a rationale.             |

Declare the permissions in the manifest as usual.

## The manager

The view creates the manager:

- Compose: `rememberEmaPermissionManager()`. See [Compose permissions](../compose/permissions.md).
- Fragments: `EmaAndroidPermissionManager(fragment)`. See [Android Views permissions](../android-view/permissions.md).

```kotlin
permissionManager.isPermissionGranted(Manifest.permission.CAMERA)   // PermissionState
permissionManager.areAllPermissionsGranted(a, b)                    // Boolean
permissionManager.shouldShowRequestPermissionRationale(permission)  // Boolean
permissionManager.requestPermission(permission)                     // suspend → PermissionState
permissionManager.requestMultiplePermission(a, b)                   // suspend → Map<String, PermissionState>
```

Make one request at a time. Android cancels a request made while another one is on screen: it returns `NOT_GRANTED`
(an empty map for several permissions) instead of waiting.

### Location

There are shortcuts for location: `requestCoarseLocationPermission()`, `requestFineLocationPermission()`,
`isLocationCoarseGranted()`, `isLocationFineGranted()` and `isLocationBackgroundGranted()`.

Request the fine location with `requestFineLocationPermission()`, not with `requestPermission`: depending on the Android
version it must be requested together with the coarse location. `requestBackgroundLocationPermission(infoDialogType)` of
`EmaAndroidPermissionManager` requests the background location, which needs the fine location first. On recent Android
versions the user grants it in the system settings, and `infoDialogType` decides the explanation shown before opening
them: `InfoDialogType.Default(title, message)`, or `InfoDialogType.CustomDialog(alertDialog, onAcceptClickListener)`
to show your own dialog.

## Asking for a permission from the ViewModel

The ViewModel does not touch Android, so it **puts the request in the state**. The view executes it.

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
    updateState { copy(permissionRequest = EmaPermissionRequest.cancelRequest()) }  // clear the request
    when (permissionState) {
        PermissionState.GRANTED -> { /* ... */ }
        PermissionState.NOT_GRANTED -> { /* ... */ }
        PermissionState.NOT_GRANTED_SHOULD_EXPLAIN -> { /* ... */ }
    }
}
```

The view calls `permissionManager.handleRequest(request, scope, permission)` every time the request in the state changes.
`EmaPermissionRequest.cancelRequest()` is the "nothing to do" request, which `handleRequest` ignores.
`EmaMultiplePermissionRequest` and `handleRequestMultiple` work the same way for several permissions.

If the process is killed while the user is in the system settings, restore the screen with
[a save state manager](../compose/save-state.md).
