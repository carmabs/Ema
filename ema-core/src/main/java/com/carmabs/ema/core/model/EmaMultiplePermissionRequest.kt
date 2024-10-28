package com.carmabs.ema.core.model

import androidx.annotation.RestrictTo
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
class EmaMultiplePermissionRequest private constructor(
    @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    val shouldRequest: Boolean,
    @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    val onPermissionResponse: ((Map<String, PermissionState>) -> Unit)
) {
    companion object {
        fun createRequest(onPermissionResponse: (Map<String, PermissionState>) -> Unit): EmaMultiplePermissionRequest {
            return EmaMultiplePermissionRequest(
                shouldRequest = true,
                onPermissionResponse = onPermissionResponse
            )
        }

        fun cancelRequest(): EmaMultiplePermissionRequest {
            return EmaMultiplePermissionRequest(
                shouldRequest = false,
                onPermissionResponse = { }
            )
        }
    }
}