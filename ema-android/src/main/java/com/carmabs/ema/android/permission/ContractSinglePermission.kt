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
internal class ContractSinglePermission : ContractPermission() {

    private var permissionId: String? = null
    private var listener: ((PermissionState) -> Unit)? = null

    val contract = { granted: Boolean ->
        permissionId?.also {
            listener?.invoke(granted.toState(shouldShowRequestPermissionRationale(it)))
        }
        Unit
    }

    fun launch(
        permission: String,
        permissionRequest: ActivityResultLauncher<String>,
        resultListener: (PermissionState) -> Unit
    ) {
        permissionId = permission
        listener = resultListener
        permissionRequest.launch(permission)
    }
}