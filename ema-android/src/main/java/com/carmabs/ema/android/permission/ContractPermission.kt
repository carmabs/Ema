package com.carmabs.ema.android.permission

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment

/**
 * Created by Carlos Mateo Benito on 2022-08-12.
 *
 * <p>
 * Copyright (c) 2022 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
internal abstract class ContractPermission {

    var activity: ComponentActivity? = null
    var fragment: Fragment? = null

    protected fun shouldShowRequestPermissionRationale(permission: String): Boolean {
        return fragment?.shouldShowRequestPermissionRationale(permission)
            ?:let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    activity?.shouldShowRequestPermissionRationale(permission)
                } else
                    false
            } ?: false
    }

}