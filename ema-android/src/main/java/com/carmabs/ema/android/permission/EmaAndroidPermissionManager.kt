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
import com.carmabs.ema.android.extension.findActivity
import com.carmabs.ema.core.extension.toScope
import com.carmabs.ema.core.manager.EmaPermissionManager
import com.carmabs.ema.core.manager.PermissionState
import com.carmabs.ema.core.manager.areAllPermissionsStateGranted
import com.carmabs.ema.core.model.EmaPermissionRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


/**
 * Created by Carlos Mateo Benito on 2020-05-07.
 *
 * <p>
 * Copyright (c) 2020 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com.com”>Carlos Mateo Benito</a>
 */
class EmaAndroidPermissionManager : EmaPermissionManager {

    private val permissionSingleRequest: ActivityResultLauncher<String>
    private val permissionMultipleRequest: ActivityResultLauncher<Array<String>>
    private val contractSinglePermission: EmaContractSinglePermission
    private val contractMultiplePermission: EmaContractMultiplePermission
    private val context: () -> Context
    private val shouldShowRequestPermissionRationaleFunction: (String) -> Boolean

    private val requestMap = HashMap<String, Job>()

    companion object {
        fun isPermissionGranted(context: Context, permission: String): Boolean {
            return ContextCompat.checkSelfPermission(context, permission) == PERMISSION_GRANTED
        }

        fun isLocationCoarseGranted(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PERMISSION_GRANTED
        }

        fun isLocationFineGranted(context: Context): Boolean {
            val finePermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PERMISSION_GRANTED

            val coarsePermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PERMISSION_GRANTED

            return coarsePermission && finePermission
        }
    }

    constructor(
        fragment: Fragment
    ) {
        this.context = { fragment.requireContext() }
        this.shouldShowRequestPermissionRationaleFunction =
            { fragment.shouldShowRequestPermissionRationale(it) }
        this.contractSinglePermission =
            EmaContractSinglePermission(shouldShowRequestPermissionRationaleFunction)
        this.contractMultiplePermission =
            EmaContractMultiplePermission(shouldShowRequestPermissionRationaleFunction)
        permissionMultipleRequest = fragment.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
            contractMultiplePermission.contract
        )

        permissionSingleRequest = fragment.registerForActivityResult(
            ActivityResultContracts.RequestPermission(), contractSinglePermission.contract
        )
    }

    constructor(
        context: Context,
        activitySinglePermissionResultLauncher: ActivityResultLauncher<String>,
        activityMultiplePermissionResultLauncher: ActivityResultLauncher<Array<String>>,
        contractSinglePermission: EmaContractSinglePermission,
        contractMultiplePermission: EmaContractMultiplePermission
    ) {
        this.context = { context }
        this.contractSinglePermission = contractSinglePermission
        this.contractMultiplePermission = contractMultiplePermission
        permissionMultipleRequest = activityMultiplePermissionResultLauncher
        permissionSingleRequest = activitySinglePermissionResultLauncher
        shouldShowRequestPermissionRationaleFunction = {
            context.findActivity().shouldShowRequestPermissionRationale(it)
        }
    }

    private constructor(
        activity: ComponentActivity
    ) {
        this.context = { activity }
        this.shouldShowRequestPermissionRationaleFunction =
            { activity.shouldShowRequestPermissionRationale(it) }
        this.contractSinglePermission =
            EmaContractSinglePermission(shouldShowRequestPermissionRationaleFunction)
        this.contractMultiplePermission =
            EmaContractMultiplePermission(shouldShowRequestPermissionRationaleFunction)
        permissionMultipleRequest = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
            this.contractMultiplePermission.contract
        )
        permissionSingleRequest = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission(), this.contractSinglePermission.contract
        )
    }

    override suspend fun requestCoarseLocationPermission(): PermissionState {
        return requestAndroidPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    override suspend fun requestFineLocationPermission(): PermissionState {
        return when {  //ANDROID 12
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                requestFineLocationPermissionApi31()
            }

            else -> {
                requestFineLocationPermissionApi30()
            }

        }
    }

    private suspend fun requestFineLocationPermissionApi31(): PermissionState {
        val map = requestAndroidMultiplePermission(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
        return map.areAllPermissionsStateGranted().toState()
    }

    private suspend fun requestFineLocationPermissionApi30(): PermissionState {
        return requestAndroidPermission(
            Manifest.permission.ACCESS_FINE_LOCATION
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

    suspend fun requestBackgroundLocationPermission(
        infoDialog: InfoDialogType
    ): PermissionState {
        return when {
            //ANDROID 11
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                requestBackgroundLocationPermissionApi30(infoDialog)
            }
            //ANDROID 10
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                requestBackgroundLocationPermissionApi29()
            }

            else -> {
                requestBackgroundLocationPermissionApi28()
            }
        }
    }


    private suspend fun requestBackgroundLocationPermissionApi28(): PermissionState {
        val map = requestAndroidMultiplePermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        return map.areAllPermissionsStateGranted().toState()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun requestBackgroundLocationPermissionApi29(): PermissionState {
        val map = requestAndroidMultiplePermission(
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        return map.areAllPermissionsStateGranted().toState()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun requestBackgroundLocationPermissionApi30(
        dialog: InfoDialogType,
    ): PermissionState {
        val scope = coroutineContext.toScope()
        val state = requestFineLocationPermission()
        return if (state == PermissionState.GRANTED) {
            suspendCoroutine { emitter ->
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
                                            scope.launch {
                                                emitter.resume(
                                                    requestAndroidPermission(
                                                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                            }
                        }

                        is InfoDialogType.Default -> {
                            val title: String = dialog.title
                            val message: String = dialog.message
                            AlertDialog.Builder(context())
                                .setPositiveButton(R.string.ema_permission_manager_accept) { dialog, _ ->
                                    dialog.dismiss()
                                    scope.launch {
                                        emitter.resume(
                                            requestAndroidPermission(
                                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                                            )
                                        )
                                    }
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
                    emitter.resume(state)
                }
            }
        } else {
            state
        }
    }

    override suspend fun requestPermission(
        permission: String
    ): PermissionState {
        return requestPermissionWithLocationCheck(permission)
    }

    override suspend fun requestMultiplePermission(
        vararg permission: String
    ): Map<String, PermissionState> {
        return requestMultiplePermissionWithLocationCheck(
            *permission
        )
    }

    private suspend fun requestAndroidPermission(
        permission: String
    ): PermissionState {
        checkPermissionInManifest(permission)
        return if (isPermissionGranted(permission) == PermissionState.GRANTED)
            PermissionState.GRANTED
        else {
            contractSinglePermission.launch(permission, permissionSingleRequest)
        }
    }

    private suspend fun requestAndroidMultiplePermission(
        vararg permission: String
    ): Map<String, PermissionState> {
        checkPermissionInManifest(*permission)
        return if (areAllPermissionsGranted(*permission)) {
            val map = hashMapOf<String, PermissionState>()
            permission.forEach {
                map[it] = PermissionState.GRANTED
            }
            map
        } else {
            contractMultiplePermission.launch(
                permissionMultipleRequest,
                *permission
            )
        }
    }

    private suspend fun requestPermissionWithLocationCheck(
        permission: String,
    ): PermissionState {
        return when (val check = checkLocationPermission(permission)) {
            is LocationPermissionCheck.CallRequestBackground ->
                throw RuntimeException(check.message)

            is LocationPermissionCheck.CallRequestFineLocation -> requestFineLocationPermission()

            LocationPermissionCheck.Continue -> requestAndroidPermission(permission)

        }
    }

    private suspend fun requestMultiplePermissionWithLocationCheck(
        vararg permission: String
    ): Map<String, PermissionState> {
        return when (val check = checkLocationPermission(*permission)) {
            is LocationPermissionCheck.CallRequestBackground -> {
                throw RuntimeException(check.message)
            }

            is LocationPermissionCheck.CallRequestFineLocation -> {
                val fineState = requestFineLocationPermission()
                val mapResultsForNotLocationFine = requestAndroidMultiplePermission(
                    *permission.filterNot {
                        val permissionsFine = getFineLocationPermission()
                        permissionsFine.contains(it)
                    }.toTypedArray()
                )

                val resultMap = hashMapOf<String, PermissionState>()
                resultMap.putAll(mapResultsForNotLocationFine)
                resultMap[Manifest.permission.ACCESS_FINE_LOCATION] = fineState
                resultMap

            }

            LocationPermissionCheck.Continue -> {
                requestAndroidMultiplePermission(*permission)
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
            isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION) != PermissionState.GRANTED && permission.contains(
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

    override fun isPermissionGranted(permission: String): PermissionState {
        val granted =
            ContextCompat.checkSelfPermission(context(), permission) == PERMISSION_GRANTED
        return granted.toState(shouldShowRequestPermissionRationale(permission))
    }

    override fun isLocationCoarseGranted(): PermissionState {
        return isLocationCoarseGranted(context()).toState(
            shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    override fun isLocationFineGranted(): PermissionState {
        return if (Companion.isLocationFineGranted(context())) {
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

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun isLocationBackgroundGrantedApi29(): PermissionState {
        val locationGranted = isLocationFineGranted()
        return if (locationGranted != PermissionState.GRANTED)
            locationGranted
        else {
            val backgroundGranted = ContextCompat.checkSelfPermission(
                context(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PERMISSION_GRANTED

            backgroundGranted.toState(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
        }
    }


    override fun areAllPermissionsGranted(vararg permission: String): Boolean {
        var permissionsGranted = true
        permission.forEach {
            permissionsGranted = isPermissionGranted(it) == PermissionState.GRANTED
            if (!permissionsGranted)
                return false
        }
        return permissionsGranted
    }

    override fun shouldShowRequestPermissionRationale(
        permission: String
    ): Boolean {
        return shouldShowRequestPermissionRationaleFunction.invoke(permission)
    }

    private fun checkPermissionInManifest(vararg permissions: String) {
        val manifestPermissions = context().packageManager.getPackageInfo(
            context().packageName,
            PackageManager.GET_PERMISSIONS
        ).requestedPermissions
        permissions.forEach {
            if (!manifestPermissions.contains(it))
                throw RuntimeException("You must set <uses-permission android:name=\"$it\" /> in the manifest file")
        }

    }

    override fun handleRequest(
        request: EmaPermissionRequest,
        permission: String,
        scope: CoroutineScope
    ) {
        if (request.shouldRequest) {
            requestMap[permission]?.cancel()
            requestMap[permission] = scope.launch {
                val state = requestPermission(permission)
                request.onPermissionResponse.invoke(state)
            }
        }
    }

    override fun responseRequestAs(
        request: EmaPermissionRequest,
        permissionState: PermissionState
    ) {
        request.onPermissionResponse.invoke(permissionState)
    }
}
