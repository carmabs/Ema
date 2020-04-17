package com.carmabs.ema.android.manager

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.carmabs.ema.core.manager.PermissionManager

/**
 * Implementation to handle system permissions
 * Created by Carlos Mateo Benito on 2020-04-17.
 *
 * <p>
 * Copyright (c) 2020 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
class ContextPermissionManager private constructor(
    private val fragment: Fragment? = null,
    private val activity: Activity? = null
) : PermissionManager {

    private val map: HashMap<Int, (Boolean) -> Unit> = HashMap()
    private val permissionMap: HashMap<Int, PermissionModel> = HashMap()

    /**
     * Call this on permissionResult of fragment/activity to enable requestPermission result callback
     */
    override fun handlePermissionResults(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        var granted = true
        grantResults.forEach {
            if (it != PackageManager.PERMISSION_GRANTED) {
                granted = false
                return@forEach
            }
        }

        map[requestCode]?.invoke(grantResults.isNotEmpty() && granted)
        map.remove(requestCode)
    }

    override fun checkPermission(permissionId: Int): Boolean {
        var granted = true
        permissionMap[permissionId]?.also {
            it.manifestPermission.forEach { manifestPermission ->
                if (ContextCompat.checkSelfPermission(
                        fragment?.context ?: activity!!,
                        manifestPermission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        } ?: also { granted = false }

        return granted
    }

    /**
     * Bind an Int identification with a list of manifest permission to handle permission requests
     * @param permissionId identification of the permission
     * @param result Callback to check result
     */
    override fun requestPermission(permissionId: Int, result: (Boolean) -> Unit) {
        permissionMap[permissionId]?.also {
            val pendingPermissions = mutableListOf<String>()
            it.manifestPermission.forEach { manifestPermission ->
                if (ContextCompat.checkSelfPermission(
                        fragment?.context ?: activity!!,
                        manifestPermission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    pendingPermissions.add(manifestPermission)
                }
            }
            if (pendingPermissions.isEmpty()) {
                result(true)
            } else {
                val permissionArray = pendingPermissions.toTypedArray()
                map[permissionId] = result
                fragment?.requestPermissions(permissionArray, permissionId)
                    ?: also {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            activity?.requestPermissions(permissionArray, permissionId)
                        } else
                            result(true)
                    }
            }
        }
    }

    private class PermissionModel(
        val id: Int,
        val manifestPermission: Array<String>
    )

    companion object Builder {

        class PermissionFragment(fragment: Fragment) {

            private val permissionManager =
                ContextPermissionManager(
                    fragment,
                    null
                )

            fun addPermission(
                permissionId: Int,
                vararg manifestPermission: String
            ): PermissionFragment {

                permissionManager.permissionMap[permissionId] =
                    PermissionModel(
                        permissionId,
                        manifestPermission.toList().toTypedArray()
                    )
                return this
            }

            fun build(): PermissionManager = permissionManager
        }

        class PermissionActivity(val activity: Activity) {

            private val permissionManager =
                ContextPermissionManager(
                    null,
                    activity
                )

            /**
             * Bind an Int identification with a list of manifest permission to handle permission requests
             * @param permissionId identification of the permission
             * @param manifestPermission manifest permissions
             */
            fun addPermission(
                permissionId: Int,
                vararg manifestPermission: String
            ): PermissionActivity {

                permissionManager.permissionMap[permissionId] =
                    PermissionModel(
                        permissionId,
                        manifestPermission.toList().toTypedArray()
                    )
                return this
            }

            fun build(): PermissionManager = permissionManager
        }
    }

}