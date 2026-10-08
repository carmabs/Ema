# Compose permissions

Get the [permission manager](../guides/permissions.md) with `rememberEmaPermissionManager()`. In a preview it returns a
manager that grants everything.

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

The ViewModel puts an `EmaPermissionRequest` in the state, and the screen executes it every time it changes.
See [Asking for a permission from the ViewModel](../guides/permissions.md#asking-for-a-permission-from-the-viewmodel).

The suspend functions of the manager can also be called from a coroutine of the screen:

```kotlin
scope.launch {
    val cameraState = permissionManager.requestPermission(Manifest.permission.CAMERA)
}
```
