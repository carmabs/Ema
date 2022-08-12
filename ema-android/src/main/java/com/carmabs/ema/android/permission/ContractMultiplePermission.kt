package com.carmabs.ema.android.permission

import androidx.activity.result.ActivityResultLauncher
import com.carmabs.ema.core.manager.PermissionState

/**
 * Created by Carlos Mateo Benito on 2022-08-12.
 *
 * <p>
 * Copyright (c) 2022 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
internal class ContractMultiplePermission : ContractPermission() {

    private var listener: ((Map<String, PermissionState>) -> Unit)? = null

    val contract = { permissionsResult: Map<String, Boolean> ->
        val resultMap = hashMapOf<String, PermissionState>()
        permissionsResult.iterator().forEach { (key, value) ->
            resultMap[key] = value.toState(shouldShowRequestPermissionRationale(key))
        }
        listener?.invoke(resultMap)
        Unit
    }

    fun launch(
        permissionRequest: ActivityResultLauncher<Array<String>>,
        resultListener: (Map<String, PermissionState>) -> Unit,
        vararg permissions: String
    ) {
        listener = resultListener
        permissionRequest.launch(permissions.map { it }.toTypedArray())
    }
}