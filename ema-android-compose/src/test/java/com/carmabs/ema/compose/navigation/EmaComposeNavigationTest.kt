package com.carmabs.ema.compose.navigation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.carmabs.ema.android.initializer.EmaInitializerBundle
import com.carmabs.ema.android.initializer.bundle.strategy.BundleSerializerStrategy
import com.carmabs.ema.android.savestate.EmaSaveStateManager
import com.carmabs.ema.compose.CounterEvent
import com.carmabs.ema.compose.CounterScreen
import com.carmabs.ema.compose.CounterState
import com.carmabs.ema.compose.CounterViewModel
import com.carmabs.ema.compose.RouteInitializer
import com.carmabs.ema.compose.RouteInitializerSerializer
import com.carmabs.ema.compose.SilentScreen
import com.carmabs.ema.compose.extension.navigate
import com.carmabs.ema.compose.extension.navigateBack
import com.carmabs.ema.compose.extension.navigateToExternalLink
import com.carmabs.ema.compose.extension.routeId
import com.carmabs.ema.compose.extension.routeWithInitializer
import com.carmabs.ema.compose.extension.createComposableScreen
import com.carmabs.ema.compose.initializer.EmaInitializerSupport
import com.carmabs.ema.core.navigator.EmaNavigationNode
import com.carmabs.ema.core.state.EmaEvent
import com.carmabs.ema.core.viewmodel.EmaViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

sealed interface Destination : EmaEvent {
    data object Home : Destination
    data object Detail : Destination
}

class TestNodeNavigator(activity: Activity, navController: NavHostController) :
    EmaComposableNodeNavigator<Destination>(activity, navController) {
    val navigated = mutableListOf<Destination>()

    override fun onNavigation(navigationEvent: Destination): Boolean {
        navigated += navigationEvent
        navController.navigate(if (navigationEvent == Destination.Home) "home" else "detail")
        return true
    }

    val hostActivity get() = activity
}

@RunWith(RobolectricTestRunner::class)
class EmaComposeNavigationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val strategy = BundleSerializerStrategy.kSerialization(RouteInitializerSerializer)
    private val noInitializer: EmaInitializerBundle? = null

    private lateinit var navController: NavHostController
    private var viewModel: CounterViewModel? = null
    private val instances = mutableListOf<EmaViewModel<*, *>>()
    private val received = mutableListOf<CounterEvent>()
    private var saveStateSetups = 0

    private fun setNavHost(initializerSupport: EmaInitializerSupport? = EmaInitializerSupport(strategy)) {
        composeRule.setContent {
            navController = rememberNavController()
            NavHost(navController, startDestination = "home") {
                composable("home") { BasicText("Home") }
                createComposableScreen(
                    screenContent = CounterScreen(),
                    viewModel = { CounterViewModel().also { viewModel = it } },
                    initializerSupport = initializerSupport,
                    saveStateManager = EmaSaveStateManager<CounterState, CounterEvent> { _, _, _ -> saveStateSetups++ },
                    onViewModelInstance = { instances += it },
                    transitionAnimation = EmaComposableTransitions(enterTransition = { fadeIn() }),
                    decoration = { content, _ -> Box { content() } },
                    onEvent = { received += it }
                )
                createComposableScreen(
                    screenContent = SilentScreen(),
                    viewModel = { CounterViewModel() },
                    fullScreenDialogMode = true,
                    onEvent = {}
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun onMain(action: () -> Unit) {
        composeRule.runOnUiThread(action)
        composeRule.waitForIdle()
    }

    @Test
    fun `a composable screen receives the initializer of its route`() {
        setNavHost()
        val initializer = RouteInitializer("Tom & Jerry #1? 50%/100")

        onMain { navController.navigate(CounterScreen::class.routeId, EmaInitializerBundle(initializer, strategy)) }

        composeRule.onNodeWithText("Count 0").assertIsDisplayed()
        assertEquals(initializer, viewModel?.initializer)
        assertSame<Any?>(viewModel, instances.last())
        assertEquals(1, saveStateSetups)
    }

    @Test
    fun `a composable screen can be opened without initializer`() {
        setNavHost(initializerSupport = null)

        onMain { navController.navigate(CounterScreen::class.routeId, initializerBundle = noInitializer) }

        composeRule.onNodeWithText("Count 0").assertIsDisplayed()
        assertNull(viewModel?.initializer)
    }

    @Test
    fun `the initializer support can override the initializer of the route`() {
        setNavHost(EmaInitializerSupport.kSerialization(RouteInitializerSerializer, RouteInitializer("override")))

        onMain { navController.navigate(CounterScreen::class.routeId, EmaInitializerBundle(RouteInitializer("route"), strategy)) }

        assertEquals(RouteInitializer("override"), viewModel?.initializer)
    }

    @Test
    fun `a composable screen can be shown as a full screen dialog`() {
        setNavHost()

        onMain { navController.navigate(SilentScreen::class.routeId, noInitializer) { launchSingleTop = true } }

        composeRule.onNodeWithText("Silent 0").assertIsDisplayed()
    }

    @Test
    fun `navigating back pops the screens and finishes the activity at the root`() {
        setNavHost()
        onMain { navController.navigate(CounterScreen::class.routeId, initializerBundle = noInitializer) }

        var popped = false
        onMain { popped = navController.navigateBack() }
        assertTrue(popped)
        composeRule.onNodeWithText("Home").assertIsDisplayed()

        onMain { popped = navController.navigateBack(closeActivityWhenBackstackIsEmpty = false) }
        assertFalse(popped)
        assertFalse(composeRule.activity.isFinishing)

        onMain { popped = navController.navigateBack() }
        assertFalse(popped)
        assertTrue(composeRule.activity.isFinishing)
    }

    @Test
    fun `external links are opened in another activity`() {
        setNavHost()
        var opened = false

        onMain { opened = navController.navigateToExternalLink("https://github.com/carmabs") }

        assertTrue(opened)
        val intent = shadowOf(composeRule.activity).nextStartedActivity
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals("https://github.com/carmabs", intent.dataString)
    }

    @Test
    fun `the routes include the serialized initializer`() {
        assertEquals("route", routeWithInitializer("route", null))
        assertTrue(
            routeWithInitializer("route", EmaInitializerBundle(RouteInitializer("ema"), strategy))
                .startsWith("route?")
        )
        assertEquals("screen/${CounterScreen::class.java.name}", CounterScreen::class.routeId)
    }

    @Test
    fun `the node navigator navigates forward and back through the nodes`() {
        lateinit var navigator: TestNodeNavigator
        composeRule.setContent {
            navController = rememberNavController()
            navigator = rememberEmaNodeNavigator(navController) { activity, controller ->
                TestNodeNavigator(activity, controller)
            } as TestNodeNavigator
            NavHost(navController, startDestination = "home") {
                composable("home") { BasicText("Home") }
                composable("detail") { BasicText("Detail") }
            }
        }
        composeRule.waitForIdle()
        val home = EmaNavigationNode<Destination>(Destination.Home)
        val detail = home.next(Destination.Detail)
        val notified = mutableListOf<Destination>()

        onMain { navigator.navigate(detail) { notified += it } }
        composeRule.onNodeWithText("Detail").assertIsDisplayed()

        onMain { navigator.navigate(detail) }
        assertEquals(listOf<Destination>(Destination.Detail), navigator.navigated)
        assertEquals(listOf<Destination>(Destination.Detail), notified)

        onMain { navigator.navigate(home) }
        composeRule.onNodeWithText("Home").assertIsDisplayed()
        assertEquals(listOf<Destination>(Destination.Detail), navigator.navigated)
        assertSame(composeRule.activity, navigator.hostActivity)

        var popped = true
        onMain { popped = navigator.navigateBack(closeActivityWhenBackstackIsEmpty = false) }
        assertFalse(popped)
        assertFalse(composeRule.activity.isFinishing)
        onMain { navigator.navigateBack() }
        assertTrue(composeRule.activity.isFinishing)
    }

    @Test
    fun `the initializer nav type stores the initializer in the bundle and in the route`() {
        val navType = EmaInitializerNavType(strategy)
        val bundle = Bundle()

        navType.put(bundle, "key", RouteInitializer("ema"))

        assertEquals(RouteInitializer("ema"), navType.get(bundle, "key"))
        assertEquals(RouteInitializer("ema"), navType.parseValue(navType.serializeAsValue(RouteInitializer("ema"))))
    }

    @Test
    fun `the pop transitions default to the enter and exit transitions`() {
        val empty = EmaComposableTransitions()
        assertNull(empty.enterTransition)
        assertNull(empty.popExitTransition)

        val transitions = EmaComposableTransitions(enterTransition = { fadeIn() }, exitTransition = { null })
        assertSame(transitions.enterTransition, transitions.popEnterTransition)
        assertSame(transitions.exitTransition, transitions.popExitTransition)
        assertNotNull(EmaInitializerSupport(strategy).serializerStrategy)
    }
}
