package com.carmabs.ema.android.permission

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.fragment.app.Fragment
import com.carmabs.ema.android.R
import com.carmabs.ema.core.manager.PermissionManager
import com.carmabs.ema.core.manager.PermissionState
import com.carmabs.ema.core.manager.areAllPermissionsStateGranted


/**
 * Created by Carlos Mateo Benito on 2020-05-07.
 *
 * <p>
 * Copyright (c) 2020 by atSistemas. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:cmateo.benito@atsistemas.com”>Carlos Mateo Benito</a>
 */
class EmaAndroidPermissionManager : PermissionManager {

    private var fragment: Fragment? = null
    private var activity: ComponentActivity? = null
    private val permissionSingleRequest: ActivityResultLauncher<String>
    private val permissionMultipleRequest: ActivityResultLauncher<Array<String>>
    private val contractSinglePermission = ContractSinglePermission()
    private val contractMultiplePermission = ContractMultiplePermission()
    private val context: Context by lazy {
        fragment?.requireContext() ?: activity!!
    }

    constructor(
        fragment: Fragment
    ) {
        this.fragment = fragment
        contractSinglePermission.fragment = fragment
        contractMultiplePermission.fragment = fragment
        permissionMultipleRequest = fragment.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
            contractMultiplePermission.contract
        )

        permissionSingleRequest = fragment.registerForActivityResult(
            ActivityResultContracts.RequestPermission(), contractSinglePermission.contract
        )

    }

    constructor(
        activity: ComponentActivity
    ) {
        this.activity = activity
        contractSinglePermission.activity = activity
        contractMultiplePermission.activity = activity
        permissionMultipleRequest = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
            contractMultiplePermission.contract
        )
        permissionSingleRequest = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission(), contractSinglePermission.contract
        )
    }

    override fun requestCoarseLocationPermission(
        resultListener: (PermissionState) -> Unit
    ) {
        requestAndroidPermission(Manifest.permission.ACCESS_COARSE_LOCATION, resultListener)
    }

    override fun requestFineLocationPermission(
        resultListener: (PermissionState) -> Unit
    ) {
        when {  //ANDROID 12
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                requestFineLocationPermissionApi31(resultListener)
            }
            else -> {
                requestFineLocationPermissionApi30(resultListener)
            }

        }
    }

    private fun requestFineLocationPermissionApi31(resultListener: (PermissionState) -> Unit) {
        requestAndroidMultiplePermission(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            resultListener = {
                resultListener(it.areAllPermissionsStateGranted().toState())
            }
        )
    }

    private fun requestFineLocationPermissionApi30(resultListener: (PermissionState) -> Unit) {
        requestAndroidPermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
            resultListener = {
                resultListener(it)
            }
        )
    }

    private fun getFineLocationPermission(): MutableList<String> {
        val listPermissions = mutableListOf<String>()
        when {
            //Android 12
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                listPermissions.addAll(getFineLocationPermissionApi31())
            }
            else -> {
                listPermissions.add(getFineLocationPermissionApi30())
            }
        }
        return listPermissions
    }

    private fun getFineLocationPermissionApi31(): List<String> {
        return listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun getFineLocationPermissionApi30(): String {
        return Manifest.permission.ACCESS_FINE_LOCATION
    }

    fun requestBackgroundLocationPermission(
        infoDialog: InfoDialogType,
        resultListener: (PermissionState) -> Unit
    ) {
        when {
            //ANDROID 11
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                requestBackgroundLocationPermissionApi30(infoDialog, resultListener)
            }
            //ANDROID 10
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                requestBackgroundLocationPermissionApi29(resultListener)
            }
            else -> {
                requestBackgroundLocationPermissionApi28(resultListener)
            }
        }
    }


    private fun requestBackgroundLocationPermissionApi28(resultListener: (PermissionState) -> Unit) {
        requestAndroidMultiplePermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            resultListener = {
                resultListener(it.areAllPermissionsStateGranted().toState())
            }
        )
    }

    private fun requestBackgroundLocationPermissionApi29(resultListener: (PermissionState) -> Unit) {
        requestAndroidMultiplePermission(
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            resultListener = {
                resultListener(it.areAllPermissionsStateGranted().toState())
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundLocationPermissionApi30(
        dialog: InfoDialogType,
        resultListener: (PermissionState) -> Unit
    ) {
        requestFineLocationPermission {
            if (it == PermissionState.GRANTED) {
                val permissionBackground = isLocationBackgroundGranted()
                if (permissionBackground == PermissionState.NOT_GRANTED_SHOULD_EXPLAIN) {
                    val alertDialog = when (dialog) {
                        is InfoDialogType.CustomDialog -> {
                            dialog.alertDialog.apply {
                                setOnShowListener {
                                    getButton(Dialog.BUTTON_POSITIVE)?.apply {
                                        setOnClickListener {
                                            dialog.onAcceptClickListener?.invoke()
                                            dismiss()
                                            requestAndroidPermission(
                                                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                                                resultListener
                                            )
                                        }
                                    }
                                }

                            }
                        }
                        is InfoDialogType.Default -> {
                            val title: String = dialog.title
                            val message: String = dialog.message
                            AlertDialog.Builder(context)
                                .setPositiveButton(R.string.ema_permission_manager_accept) { dialog, _ ->
                                    dialog.dismiss()
                                    requestAndroidPermission(
                                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                                        resultListener
                                    )
                                }
                                .setTitle(title)
                                .setMessage(message)
                                .setNegativeButton(R.string.ema_permission_manager_cancel) { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .create()
                        }
                    }

                    alertDialog.show()
                } else {
                    resultListener.invoke(it)
                }
            } else
                resultListener.invoke(it)
        }
    }

    override fun requestPermission(
        permission: String,
        resultListener: (PermissionState) -> Unit
    ) {
        requestPermissionWithLocationCheck(permission, resultListener)
    }

    override fun requestMultiplePermission(
        vararg permission: String,
        resultListener: (Map<String, PermissionState>) -> Unit
    ) {
        requestMultiplePermissionWithLocationCheck(*permission, resultListener = resultListener)
    }

    private fun requestAndroidPermission(
        permission: String,
        resultListener: (PermissionState) -> Unit
    ) {
        checkPermissionInManifest(permission)
        if (isPermissionsGranted(permission) == PermissionState.GRANTED)
            resultListener.invoke(PermissionState.GRANTED)
        else {
            contractSinglePermission.launch(permission, permissionSingleRequest, resultListener)
        }
    }

    private fun requestAndroidMultiplePermission(
        vararg permission: String,
        resultListener: (Map<String, PermissionState>) -> Unit
    ) {
        checkPermissionInManifest(*permission)
        if (areAllPermissionsGranted(*permission)) {
            val map = hashMapOf<String, PermissionState>()
            permission.forEach {
                map[it] = PermissionState.GRANTED
            }
            resultListener.invoke(map)
        } else {
            contractMultiplePermission.launch(
                permissionMultipleRequest,
                resultListener,
                *permission
            )
        }
    }

    private fun requestPermissionWithLocationCheck(
        permission: String,
        resultListener: (PermissionState) -> Unit
    ) {
        when (val check = checkLocationPermission(permission)) {
            is LocationPermissionCheck.CallRequestBackground ->
                throw RuntimeException(check.message)
            is LocationPermissionCheck.CallRequestFineLocation -> requestFineLocationPermission(
                resultListener
            )
            LocationPermissionCheck.Continue -> requestAndroidPermission(
                permission,
                resultListener = resultListener
            )

        }
    }

    private fun requestMultiplePermissionWithLocationCheck(
        vararg permission: String,
        resultListener: (Map<String, PermissionState>) -> Unit
    ) {
        when (val check = checkLocationPermission(*permission)) {
            is LocationPermissionCheck.CallRequestBackground -> {
                throw RuntimeException(check.message)
            }
            is LocationPermissionCheck.CallRequestFineLocation -> {
                requestFineLocationPermission { fineState ->
                    requestAndroidMultiplePermission(
                        *permission.filterNot {
                            val permissionsFine = getFineLocationPermission()
                            permissionsFine.contains(it)
                        }.toTypedArray(),
                        resultListener = {
                            val mapResultsForNotLocationFine = it
                            val resultMap = hashMapOf<String, PermissionState>()
                            resultMap.putAll(mapResultsForNotLocationFine)
                            resultMap[Manifest.permission.ACCESS_FINE_LOCATION] = fineState
                            resultListener.invoke(resultMap)
                        }
                    )
                }
            }
            LocationPermissionCheck.Continue -> {
                requestAndroidMultiplePermission(*permission, resultListener = resultListener)
            }
        }
    }

    private fun checkLocationPermission(
        vararg permission: String,
    ): LocationPermissionCheck {
        val check: LocationPermissionCheck = when {
            //Different behaviour dependent of OS Version
            permission.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION) -> {
                LocationPermissionCheck.CallRequestBackground(this.javaClass.name)
            }
            //Ok, assure fine location in all devices
            permission.contains(Manifest.permission.ACCESS_FINE_LOCATION) && permission.contains(
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) -> {
                LocationPermissionCheck.Continue
            }
            //Different behaviour dependent of OS Version
            isPermissionsGranted(Manifest.permission.ACCESS_COARSE_LOCATION) != PermissionState.GRANTED && permission.contains(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                LocationPermissionCheck.CallRequestFineLocation(this.javaClass.name)
            }
            else ->
                LocationPermissionCheck.Continue

        }

        return check
    }

    private sealed interface LocationPermissionCheck {
        data class CallRequestFineLocation(
            private val classContainerName: String,
            val message: String = "Please, use the requestBackgroundLocationPermission method of the $classContainerName to handle this permission " +
                    "properly with all android versions "
        ) : LocationPermissionCheck

        data class CallRequestBackground(
            private val classContainerName: String,
            val message: String = "Please, use the requestFineLocationPermission of the $classContainerName class to handle this permission " +
                    "properly with all android versions "
        ) : LocationPermissionCheck

        object Continue : LocationPermissionCheck
    }

    override fun isPermissionsGranted(permission: String): PermissionState {
        val granted =
            ContextCompat.checkSelfPermission(context, permission) == PERMISSION_GRANTED
        return granted.toState(shouldShowRequestPermissionRationale(permission))
    }

    override fun isLocationCoarseGranted(): PermissionState {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PERMISSION_GRANTED

        return granted.toState(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    override fun isLocationFineGranted(): PermissionState {
        val finePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PERMISSION_GRANTED

        val coarsePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PERMISSION_GRANTED

        return if (coarsePermission && finePermission) {
            PermissionState.GRANTED
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) || shouldShowRequestPermissionRationale(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
                PermissionState.NOT_GRANTED_SHOULD_EXPLAIN
            else
                PermissionState.NOT_GRANTED
        }
    }

    override fun isLocationBackgroundGranted(): PermissionState {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                isLocationBackgroundGrantedApi29()
            }
            else -> {
                isLocationBackgroundGrantedApi28()
            }
        }
    }

    private fun isLocationBackgroundGrantedApi28(): PermissionState {
        return isLocationFineGranted()
    }

    private fun isLocationBackgroundGrantedApi29(): PermissionState {
        val locationGranted = isLocationFineGranted()
        return if (locationGranted != PermissionState.GRANTED)
            locationGranted
        else {
            val backgroundGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PERMISSION_GRANTED

            backgroundGranted.toState(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
        }
    }


    override fun areAllPermissionsGranted(vararg permission: String): Boolean {
        var permissionsGranted = true
        permission.forEach {
            permissionsGranted = isPermissionsGranted(it) == PermissionState.GRANTED
            if (!permissionsGranted)
                return false
        }
        return permissionsGranted
    }

    override fun shouldShowRequestPermissionRationale(permission: String): Boolean {
        return fragment?.shouldShowRequestPermissionRationale(permission)
            ?: let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    activity?.shouldShowRequestPermissionRationale(permission)
                } else
                    false
            } ?: false
    }

    private fun checkPermissionInManifest(vararg permissions: String) {
        val manifestPermissions = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        ).requestedPermissions
        permissions.forEach {
            if (!manifestPermissions.contains(it))
                throw RuntimeException("You must set <uses-permission android:name=\"$it\" /> in the manifest file")
        }

    }
}
