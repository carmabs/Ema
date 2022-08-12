package com.carmabs.ema.android.permission

import com.carmabs.ema.core.manager.PermissionState

/**
 * Created by Carlos Mateo Benito on 12/8/22.
 *
 * <p>
 * Copyright (c) 2022 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
internal fun Boolean.toState(falseIsDenied:Boolean = false): PermissionState {
    return if(this)
        PermissionState.GRANTED
    else{
        if(falseIsDenied)
            PermissionState.NOT_GRANTED_SHOULD_EXPLAIN
        else
            PermissionState.NOT_GRANTED
    }
}