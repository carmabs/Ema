package com.carmabs.ema.android.permission

import android.Manifest
import android.app.AlertDialog
import android.app.Application
import android.content.DialogInterface
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.test.core.app.ApplicationProvider
import com.carmabs.ema.core.manager.PermissionState
import com.carmabs.ema.core.model.EmaMultiplePermissionRequest
import com.carmabs.ema.core.model.EmaPermissionRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowAlertDialog
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Launcher that answers synchronously, like the system does when it cancels a request.
 */
private class FakeLauncher<I>(private val onLaunch: (I) -> Unit) : ActivityResultLauncher<I>() {
    val launched = mutableListOf<I>()

    override fun launch(input: I, options: ActivityOptionsCompat?) {
        launched.add(input)
        onLaunch(input)
    }

    override fun unregister() = Unit

    override val contract: ActivityResultContract<I, *>
        get() = throw UnsupportedOperationException()
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class EmaAndroidPermissionManagerTest {

    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val coarse = Manifest.permission.ACCESS_COARSE_LOCATION
    private val fine = Manifest.permission.ACCESS_FINE_LOCATION
    private val background = Manifest.permission.ACCESS_BACKGROUND_LOCATION
    private val camera = Manifest.permission.CAMERA

    private var systemAnswer: (String) -> Boolean = { false }
    private var rationale: (String) -> Boolean = { false }
    private lateinit var singleLauncher: FakeLauncher<String>
    private lateinit var multipleLauncher: FakeLauncher<Array<String>>

    private fun TestScope.createManager(context: android.content.Context = application) =
        EmaAndroidPermissionManager(
            contextProvider = { context },
            shouldShowRequestPermissionRationale = { rationale(it) },
            registerSinglePermission = { contract ->
                FakeLauncher<String> { permission ->
                    contract.contract(systemAnswer(permission))
                }.also { singleLauncher = it }
            },
            registerMultiplePermission = { contract ->
                FakeLauncher<Array<String>> { permissions ->
                    contract.contract(permissions.associateWith(systemAnswer))
                }.also { multipleLauncher = it }
            }
        )

    @Before
    fun declarePermissionsInManifest() {
        shadowOf(application.packageManager).getInternalMutablePackageInfo(application.packageName)
            .requestedPermissions = arrayOf(camera, Manifest.permission.RECORD_AUDIO, coarse, fine, background)
    }

    private fun grant(vararg permissions: String) = shadowOf(application).grantPermissions(*permissions)

    @Test
    fun `granted permissions are not requested again`() = runTest {
        grant(camera)
        val manager = createManager()
        assertEquals(PermissionState.GRANTED, manager.requestPermission(camera))
        assertTrue(singleLauncher.launched.isEmpty())
        assertEquals(PermissionState.GRANTED, manager.isPermissionGranted(camera))
        assertTrue(manager.areAllPermissionsGranted(camera))
    }

    @Test
    fun `a missing permission is requested to the system`() = runTest {
        val manager = createManager()
        systemAnswer = { true }
        assertEquals(PermissionState.GRANTED, manager.requestPermission(camera))
        assertEquals(listOf(camera), singleLauncher.launched)
    }

    @Test
    fun `a denied permission reports if it should be explained`() = runTest {
        val manager = createManager()
        assertEquals(PermissionState.NOT_GRANTED, manager.requestPermission(camera))
        rationale = { true }
        assertEquals(PermissionState.NOT_GRANTED_SHOULD_EXPLAIN, manager.requestPermission(camera))
        assertEquals(PermissionState.NOT_GRANTED_SHOULD_EXPLAIN, manager.isPermissionGranted(camera))
        assertTrue(manager.shouldShowRequestPermissionRationale(camera))
        assertFalse(manager.areAllPermissionsGranted(camera))
    }

    @Test
    fun `several permissions are requested at once`() = runTest {
        val manager = createManager()
        systemAnswer = { it == camera }
        val result = manager.requestMultiplePermission(camera, Manifest.permission.RECORD_AUDIO)
        assertEquals(PermissionState.GRANTED, result[camera])
        assertEquals(PermissionState.NOT_GRANTED, result[Manifest.permission.RECORD_AUDIO])
    }

    @Test
    fun `several granted permissions are not requested again`() = runTest {
        grant(camera, Manifest.permission.RECORD_AUDIO)
        val manager = createManager()
        val result = manager.requestMultiplePermission(camera, Manifest.permission.RECORD_AUDIO)
        assertTrue(result.values.all { it == PermissionState.GRANTED })
        assertTrue(multipleLauncher.launched.isEmpty())
    }

    @Test
    fun `fine location asks for coarse and fine location together`() = runTest {
        val manager = createManager()
        systemAnswer = { true }
        assertEquals(PermissionState.GRANTED, manager.requestFineLocationPermission())
        assertEquals(listOf(coarse, fine), multipleLauncher.launched.single().toList())
    }

    @Test
    fun `coarse location is requested alone`() = runTest {
        val manager = createManager()
        systemAnswer = { true }
        assertEquals(PermissionState.GRANTED, manager.requestCoarseLocationPermission())
        assertEquals(listOf(coarse), singleLauncher.launched)
    }

    @Test
    fun `requesting fine location without coarse uses the fine location flow`() = runTest {
        val manager = createManager()
        systemAnswer = { true }
        assertEquals(PermissionState.GRANTED, manager.requestPermission(fine))
        assertEquals(listOf(coarse, fine), multipleLauncher.launched.single().toList())
    }

    @Test
    fun `requesting fine location in a group adds the fine location state`() = runTest {
        val manager = createManager()
        systemAnswer = { true }
        val result = manager.requestMultiplePermission(fine, camera)
        assertEquals(PermissionState.GRANTED, result[fine])
        assertEquals(PermissionState.GRANTED, result[camera])
    }

    @Test
    fun `fine and coarse location requested together are requested directly`() = runTest {
        val manager = createManager()
        systemAnswer = { true }
        val result = manager.requestMultiplePermission(coarse, fine)
        assertEquals(setOf(coarse, fine), result.keys)
    }

    @Test
    fun `background location must use its own method`() = runTest {
        val manager = createManager()
        val single = assertFailsWith<RuntimeException> { manager.requestPermission(background) }
        assertTrue(single.message!!.contains("requestBackgroundLocationPermission"))
        val multiple = assertFailsWith<RuntimeException> { manager.requestMultiplePermission(background, camera) }
        assertTrue(multiple.message!!.contains("requestBackgroundLocationPermission"))
    }

    @Test
    fun `location state checks`() = runTest {
        val manager = createManager()
        assertEquals(PermissionState.NOT_GRANTED, manager.isLocationFineGranted())
        assertEquals(PermissionState.NOT_GRANTED, manager.isLocationCoarseGranted())
        assertEquals(PermissionState.NOT_GRANTED, manager.isLocationBackgroundGranted())
        rationale = { it == fine }
        assertEquals(PermissionState.NOT_GRANTED_SHOULD_EXPLAIN, manager.isLocationFineGranted())

        grant(coarse, fine)
        assertEquals(PermissionState.GRANTED, manager.isLocationFineGranted())
        assertEquals(PermissionState.GRANTED, manager.isLocationCoarseGranted())
        assertEquals(PermissionState.NOT_GRANTED, manager.isLocationBackgroundGranted())
        grant(background)
        assertEquals(PermissionState.GRANTED, manager.isLocationBackgroundGranted())

        assertTrue(EmaAndroidPermissionManager.isPermissionGranted(application, fine))
        assertTrue(EmaAndroidPermissionManager.isLocationCoarseGranted(application))
        assertTrue(EmaAndroidPermissionManager.isLocationFineGranted(application))
    }

    @Test
    fun `background location is granted directly when it does not need an explanation`() = runTest {
        grant(coarse, fine, background)
        val manager = createManager()
        assertEquals(PermissionState.GRANTED, manager.requestBackgroundLocationPermission(InfoDialogType.Default("t", "m")))
    }

    @Test
    fun `background location shows the explanation and requests it when accepted`() = runTest {
        grant(coarse, fine)
        rationale = { it == background }
        systemAnswer = { true }
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()
        val manager = createManager(activity)

        val result = async { manager.requestBackgroundLocationPermission(InfoDialogType.Default("Title", "Message")) }
        runCurrent()
        shadowOf(android.os.Looper.getMainLooper()).idle()
        val dialog = ShadowAlertDialog.getLatestAlertDialog() as AlertDialog
        assertEquals("Message", shadowOf(dialog).message)
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
        shadowOf(android.os.Looper.getMainLooper()).idle()
        advanceUntilIdle()

        assertEquals(PermissionState.GRANTED, result.await())
        assertEquals(listOf(background), singleLauncher.launched)
    }

    @Test
    fun `background location ends when the explanation is cancelled`() = runTest {
        grant(coarse, fine)
        rationale = { it == background }
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()
        val manager = createManager(activity)

        val result = async { manager.requestBackgroundLocationPermission(InfoDialogType.Default("Title", "Message")) }
        runCurrent()
        shadowOf(android.os.Looper.getMainLooper()).idle()
        val dialog = ShadowAlertDialog.getLatestAlertDialog() as AlertDialog
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick()
        shadowOf(android.os.Looper.getMainLooper()).idle()

        assertEquals(PermissionState.NOT_GRANTED_SHOULD_EXPLAIN, result.await())
        assertTrue(singleLauncher.launched.isEmpty())
    }

    @Test
    fun `background location with a custom dialog`() = runTest {
        grant(coarse, fine)
        rationale = { it == background }
        systemAnswer = { true }
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()
        val manager = createManager(activity)
        var accepted = false
        val customDialog = AlertDialog.Builder(activity)
            .setPositiveButton("Ok") { _, _ -> }
            .create()

        val result = async {
            manager.requestBackgroundLocationPermission(InfoDialogType.CustomDialog(customDialog) { accepted = true })
        }
        runCurrent()
        shadowOf(android.os.Looper.getMainLooper()).idle()
        customDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
        shadowOf(android.os.Looper.getMainLooper()).idle()
        advanceUntilIdle()

        assertTrue(accepted)
        assertEquals(PermissionState.GRANTED, result.await())
    }

    @Test
    fun `background location is not requested without fine location`() = runTest {
        val manager = createManager()
        assertEquals(PermissionState.NOT_GRANTED, manager.requestBackgroundLocationPermission(InfoDialogType.Default("t", "m")))
    }

    @Test
    fun `permissions missing in the manifest fail`() = runTest {
        val packageInfo = shadowOf(application.packageManager).getInternalMutablePackageInfo(application.packageName)
        packageInfo.requestedPermissions = arrayOf(camera)
        val manager = createManager()
        systemAnswer = { true }
        assertEquals(PermissionState.GRANTED, manager.requestPermission(camera))
        val error = assertFailsWith<RuntimeException> { manager.requestPermission(Manifest.permission.RECORD_AUDIO) }
        assertTrue(error.message!!.contains("uses-permission"))
    }

    @Test
    fun `handleRequest requests the permission and responds to the request`() = runTest {
        val manager = createManager()
        systemAnswer = { true }
        var single: PermissionState? = null
        var multiple: Map<String, PermissionState>? = null
        val scope = CoroutineScope(coroutineContext)

        manager.handleRequest(EmaPermissionRequest.createRequest { single = it }, scope, camera)
        manager.handleRequestMultiple(EmaMultiplePermissionRequest.createRequest { multiple = it }, scope, camera, Manifest.permission.RECORD_AUDIO)
        advanceUntilIdle()

        assertEquals(PermissionState.GRANTED, single)
        assertEquals(PermissionState.GRANTED, multiple?.get(Manifest.permission.RECORD_AUDIO))
    }

    @Test
    fun `cancelled requests do nothing`() = runTest {
        val manager = createManager()
        val scope = CoroutineScope(coroutineContext)
        manager.handleRequest(EmaPermissionRequest.cancelRequest(), scope, camera)
        manager.handleRequestMultiple(EmaMultiplePermissionRequest.cancelRequest(), scope, camera)
        advanceUntilIdle()
        assertTrue(singleLauncher.launched.isEmpty())
        assertTrue(multipleLauncher.launched.isEmpty())
    }

    @Test
    fun `responseRequest answers the request directly`() = runTest {
        val manager = createManager()
        var single: PermissionState? = null
        var multiple: Map<String, PermissionState>? = null
        manager.responseRequest(EmaPermissionRequest.createRequest { single = it }, PermissionState.NOT_GRANTED)
        manager.responseRequest(EmaMultiplePermissionRequest.createRequest { multiple = it }, mapOf(camera to PermissionState.GRANTED))
        assertEquals(PermissionState.NOT_GRANTED, single)
        assertEquals(mapOf(camera to PermissionState.GRANTED), multiple)
    }

    @Test
    fun `manager created with an activity launcher uses the activity to explain`() = runTest {
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()
        val single = EmaContractSinglePermission { false }
        val multiple = EmaContractMultiplePermission { false }
        val manager = EmaAndroidPermissionManager(
            activity,
            FakeLauncher<String> { launch { single.contract(true) } },
            FakeLauncher<Array<String>> { permissions -> launch { multiple.contract(permissions.associateWith { true }) } },
            single,
            multiple
        )
        assertFalse(manager.shouldShowRequestPermissionRationale(camera))
        assertEquals(PermissionState.GRANTED, manager.requestPermission(camera))
    }
}
