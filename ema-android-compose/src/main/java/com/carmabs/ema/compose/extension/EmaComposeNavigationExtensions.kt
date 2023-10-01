package com.carmabs.ema.compose.extension

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.Navigator
import androidx.navigation.compose.composable
import com.carmabs.ema.android.viewmodel.EmaAndroidViewModel
import com.carmabs.ema.compose.action.toImmutable
import com.carmabs.ema.compose.navigation.EmaComposableTransitions
import com.carmabs.ema.compose.provider.EmaScreenProvider
import com.carmabs.ema.compose.ui.EmaComposableScreen
import com.carmabs.ema.compose.ui.EmaComposableScreenContent
import com.carmabs.ema.core.action.FeatureEmaAction
import com.carmabs.ema.core.constants.INT_ZERO
import com.carmabs.ema.core.initializer.EmaInitializer
import com.carmabs.ema.core.navigator.EmaNavigationEvent
import com.carmabs.ema.core.state.EmaDataState
import com.carmabs.ema.core.viewmodel.EmaViewModel
import kotlin.collections.set


private var mapInitializer: HashMap<String, EmaInitializer>? = null

fun NavController.navigate(
    route: String,
    initializer: EmaInitializer? = null,
    navOptionsBuilder: (NavOptionsBuilder.() -> Unit)
) {
    initializer?.also {
        putInitializer(route, initializer)
    }
    navigate(route, navOptionsBuilder)
}

fun NavController.navigate(
    route: String,
    initializer: EmaInitializer? = null,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null
) {
    initializer?.also {
        putInitializer(route, initializer)
    }
    navigate(route, navOptions, navigatorExtras)
}

fun <S : EmaDataState, D : EmaNavigationEvent, A : FeatureEmaAction, VM : EmaAndroidViewModel<S, D>> NavGraphBuilder.createComposableScreen(
    screenContent: EmaComposableScreenContent<S, A>,
    onNavigationEvent: (D) -> Unit,
    onNavigationBackEvent: () -> Unit,
    routeId: String = screenContent::class.routeId(),
    overrideInitializer: EmaInitializer? = null,
    androidViewModel: @Composable () -> VM,
    onViewModelInstance: (@Composable (EmaViewModel<S, D>) -> Unit)? = null,
    emaComposableTransitions: EmaComposableTransitions = EmaComposableTransitions()
) {
    composable(
        routeId,
        enterTransition = emaComposableTransitions.enterTransition,
        exitTransition = emaComposableTransitions.exitTransition,
        popEnterTransition = emaComposableTransitions.popEnterTransition,
        popExitTransition = emaComposableTransitions.popExitTransition
    ) { navBack ->
        val viewModel = EmaScreenProvider.provideComposableViewModel(androidViewModel = androidViewModel.invoke())
        val vmActions = viewModel.asActionDispatcher<A>().toImmutable()

        onViewModelInstance?.invoke(viewModel)
        EmaComposableScreen(
            initializer = overrideInitializer ?: getAndReleaseInitializer(routeId),
            onNavigationEvent = onNavigationEvent,
            onNavigationBackEvent = onNavigationBackEvent,
            vm = viewModel,
            actions = vmActions,
            screenContent = screenContent
        )
    }
}

fun NavController.getInitializer(route: String? = null): EmaInitializer? {
    return currentDestination?.route?.let {
        getAndReleaseInitializer(it)
    }
}

private fun putInitializer(route: String, initializer: EmaInitializer) {
    val map = mapInitializer ?: hashMapOf<String, EmaInitializer>().apply {
        mapInitializer = this
    }
    map[route] = initializer
}

private fun getAndReleaseInitializer(route: String): EmaInitializer? {
    val initializer = mapInitializer?.remove(route)
    if (mapInitializer?.size == INT_ZERO) {
        mapInitializer = null
    }
    return initializer
}
