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
fun Map<String,Boolean>.isPermissionGranted(permission:String):Boolean{
    return this[permission]?:false
}

fun Map<String, PermissionState>.isPermissionStateGranted(permission:String):Boolean{
    return this[permission] == PermissionState.GRANTED
}

fun Map<String,Boolean>.areAllPermissionsGranted():Boolean{
    val permissions = this
    var granted = true
    if (permissions.isEmpty())
        granted = false
    else
        permissions.iterator().forEach { entry ->
            granted = granted && entry.value
        }

    return granted
}

fun Map<String, PermissionState>.areAllPermissionsStateGranted():Boolean{
    val permissions = this
    var granted = true
    if (permissions.isEmpty())
        granted = false
    else
        permissions.iterator().forEach { entry->
            granted = granted && entry.value == PermissionState.GRANTED
        }

    return granted
}