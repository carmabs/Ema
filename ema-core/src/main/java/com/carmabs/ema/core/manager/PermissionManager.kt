package com.carmabs.ema.core.manager

/**
 * Created by Carlos Mateo Benito on 2022-08-12.
 *
 * <p>
 * Copyright (c) 2022 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
interface PermissionManager {

    fun requestPermission(permission: String, resultListener: (PermissionState) -> Unit)

    fun requestMultiplePermission(
        vararg permission: String,
        resultListener: (Map<String, PermissionState>) -> Unit
    )

    fun isPermissionsGranted(permission: String): PermissionState

    fun areAllPermissionsGranted(vararg permission: String): Boolean

    fun shouldShowRequestPermissionRationale(permission:String): Boolean

    fun requestCoarseLocationPermission(resultListener: (PermissionState) -> Unit)

    fun requestFineLocationPermission(resultListener: (PermissionState) -> Unit)

    fun isLocationFineGranted(): PermissionState

    fun isLocationBackgroundGranted(): PermissionState

    fun isLocationCoarseGranted(): PermissionState
}