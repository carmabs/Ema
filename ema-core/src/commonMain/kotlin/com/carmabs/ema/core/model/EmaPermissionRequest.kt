package com.carmabs.ema.core.model

import com.carmabs.ema.core.EmaInternalApi
import com.carmabs.ema.core.manager.PermissionState

/**
 * Created by Carlos Mateo Benito on 20/6/23.
 *
 * <p>
 * Copyright (c) 2023 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
class EmaPermissionRequest private constructor(
    @EmaInternalApi
    val shouldRequest: Boolean,
    @EmaInternalApi
    val onPermissionResponse: ((PermissionState) -> Unit)
) {
    companion object {
        fun createRequest(onPermissionResponse: (PermissionState) -> Unit): EmaPermissionRequest = EmaPermissionRequest(
            shouldRequest = true,
            onPermissionResponse = onPermissionResponse
        )

        fun cancelRequest(): EmaPermissionRequest = EmaPermissionRequest(
            shouldRequest = false,
            onPermissionResponse = { }
        )
    }
}
