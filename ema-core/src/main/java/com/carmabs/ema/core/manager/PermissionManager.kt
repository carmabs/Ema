package com.carmabs.ema.core.manager

/**
 * Abstraction to handle system permissions
 * Created by Carlos Mateo Benito on 2020-04-17.
 *
 * <p>
 * Copyright (c) 2020 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
*/
interface PermissionManager {
    fun checkPermission(permissionId: Int):Boolean
    fun handlePermissionResults(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    fun requestPermission(permissionId: Int, result: (Boolean) -> Unit)
}