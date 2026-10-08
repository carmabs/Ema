# Permissions in Fragments

Create the [permission manager](../guides/permissions.md) of a Fragment as a **property**, not inside a callback:
the activity result launchers must be registered before the fragment is started.

```kotlin
private val permissionManager = EmaAndroidPermissionManager(this)
```

The ViewModel puts an `EmaPermissionRequest` in the state, and the fragment executes it every time it changes:

```kotlin
override fun LocationFragmentBinding.onState(state: LocationState) {
    bindForUpdate(state::permissionRequest) {
        permissionManager.handleRequest(
            request = it,
            scope = viewScope,
            permission = Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}
```

See [Asking for a permission from the ViewModel](../guides/permissions.md#asking-for-a-permission-from-the-viewmodel).
`viewScope` is the coroutine scope of the fragment view.

The suspend functions of the manager can also be called from a coroutine of the fragment:

```kotlin
viewScope.launch {
    val cameraState = permissionManager.requestPermission(Manifest.permission.CAMERA)
}
```
