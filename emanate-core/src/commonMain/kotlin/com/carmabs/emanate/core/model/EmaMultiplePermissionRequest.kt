package com.carmabs.emanate.core.model

import com.carmabs.emanate.core.manager.PermissionState

/**
 * Created by Carlos Mateo Benito on 20/6/23.
 *
 * <p>
 * Copyright (c) 2023 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
class EmaMultiplePermissionRequest private constructor(
    /**
     * False when the request is cancelled: the permission manager must not request the permission
     */
    val shouldRequest: Boolean,
    /**
     * Called by the permission manager with the result of the request
     */
    val onPermissionResponse: ((Map<String, PermissionState>) -> Unit)
) {
    companion object {
        fun createRequest(onPermissionResponse: (Map<String, PermissionState>) -> Unit): EmaMultiplePermissionRequest =
            EmaMultiplePermissionRequest(
                shouldRequest = true,
                onPermissionResponse = onPermissionResponse
            )

        fun cancelRequest(): EmaMultiplePermissionRequest = EmaMultiplePermissionRequest(
            shouldRequest = false,
            onPermissionResponse = { }
        )
    }
}
