# Permissions

`EmaPermissionManager` wraps the Android permission flow in `suspend` functions and a small request model that fits
the state/ViewModel flow. The permission result is one of three values:

| `PermissionState`             | Meaning                                                              |
|-------------------------------|----------------------------------------------------------------------|
| `GRANTED`                     | The permission is granted.                                           |
| `NOT_GRANTED`                 | Not granted.                                                         |
| `NOT_GRANTED_SHOULD_EXPLAIN`  | Not granted, and Android recommends showing a rationale.             |

Declare the permission in the manifest as usual.

## The manager

| Where       | How to get it                                                   |
|-------------|-----------------------------------------------------------------|
| Fragment    | `private val permissionManager = EmaAndroidPermissionManager(this)` |
| Compose     | `val permissionManager = rememberEmaPermissionManager()`        |

> In a Fragment create it as a **property**, not inside a callback. Registering the activity result launchers
> must happen before the fragment is started.

```kotlin
permissionManager.isPermissionGranted(Manifest.permission.CAMERA)   // PermissionState
permissionManager.areAllPermissionsGranted(a, b)                    // Boolean
permissionManager.shouldShowRequestPermissionRationale(permission)  // Boolean
permissionManager.requestPermission(permission)                     // suspend → PermissionState
permissionManager.requestMultiplePermission(a, b)                   // suspend → Map<String, PermissionState>
```

There are shortcuts for location: `requestCoarseLocationPermission()`, `requestFineLocationPermission()`,
`isLocationCoarseGranted()`, `isLocationFineGranted()` and `isLocationBackgroundGranted()`.

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

```kotlin
// Fragment
override fun CounterFragmentBinding.onState(state: LocationState) {
    bindForUpdate(state::permissionRequest) {
        permissionManager.handleRequest(
            request = it,
            scope = viewScope,
            permission = Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}
```

```kotlin
// Compose
val permissionManager = rememberEmaPermissionManager()
val scope = rememberCoroutineScope()
LaunchedEffect(state.permissionRequest) {
    permissionManager.handleRequest(state.permissionRequest, scope, Manifest.permission.ACCESS_COARSE_LOCATION)
}
```

`EmaPermissionRequest.cancelRequest()` is the "nothing to do" request. `EmaMultiplePermissionRequest` and
`handleRequestMultiple` work the same way for several permissions.

If the process is killed while the user is in the system settings, restore the screen with
[a save state manager](save-state.md).
