# Permisos en Fragments

Crea el [gestor de permisos](../guides/permissions.es.md) de un Fragment como una **propiedad**, no dentro de un callback:
los launchers de resultados de actividad tienen que registrarse antes de que arranque el fragment.

```kotlin
private val permissionManager = EmaAndroidPermissionManager(this)
```

El ViewModel mete un `EmaPermissionRequest` en el estado, y el fragment lo ejecuta cada vez que cambia:

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

Consulta [Pedir un permiso desde el ViewModel](../guides/permissions.es.md#pedir-un-permiso-desde-el-viewmodel).
`viewScope` es el scope de corrutinas de la vista del fragment.

Las funciones suspendidas del gestor también se pueden llamar desde una corrutina del fragment:

```kotlin
viewScope.launch {
    val cameraState = permissionManager.requestPermission(Manifest.permission.CAMERA)
}
```
