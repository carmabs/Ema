package com.carmabs.ema.compose.ui

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.carmabs.ema.android.viewmodel.EmaAndroidViewModel
import com.carmabs.ema.compose.action.EmaImmutableActionDispatcher
import com.carmabs.ema.compose.action.EmaImmutableActionDispatcherEmpty
import com.carmabs.ema.compose.action.toImmutable
import com.carmabs.ema.compose.extension.asActionDispatcher
import com.carmabs.ema.compose.extension.skipForPreview
import com.carmabs.ema.compose.provider.EmaScreenProvider
import com.carmabs.ema.core.action.EmaActionDispatcher
import com.carmabs.ema.core.action.ViewModelEmaAction
import com.carmabs.ema.core.initializer.EmaInitializer
import com.carmabs.ema.core.model.EmaEvent
import com.carmabs.ema.core.navigator.EmaNavigationDirection
import com.carmabs.ema.core.navigator.EmaNavigationEvent
import com.carmabs.ema.core.navigator.onNavigation
import com.carmabs.ema.core.state.EmaDataState
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.viewmodel.EmaViewModel

@Composable
fun <S : EmaDataState, VM : EmaViewModel<S, D>, D : EmaNavigationEvent, A : ViewModelEmaAction> EmaComposableScreen(
    initializer: EmaInitializer? = null,
    vm: VM,
    actions: EmaActionDispatcher<A>,
    screenContent: EmaComposableScreenContent<S, A>,
    onNavigationEvent: (D) -> Unit,
    onNavigationBackEvent: () -> Unit,
    actionSubscriber: ((A) -> Unit)? = null,
    previewRenderState: S? = null
) {
    skipForPreview(
        previewComposable = {
            previewRenderState?.also { previewState ->
                screenContent.onStateNormal(
                    state = previewState,
                    actions = EmaImmutableActionDispatcherEmpty()
                )
            }

        }
    ) {
        val immutableActions = remember {
            actions.toImmutable()
        }

        renderScreen(
            initializer,
            screenContent,
            vm,
            immutableActions,
            onNavigationEvent,
            onNavigationBackEvent,
            actionSubscriber
        )
    }
}

@Composable
fun <S : EmaDataState, D : EmaNavigationEvent, A : ViewModelEmaAction> EmaComposableScreen(
    initializer: EmaInitializer? = null,
    vm: () -> EmaAndroidViewModel<S, D>,
    screenContent: EmaComposableScreenContent<S, A>,
    onNavigationEvent: (D) -> Unit,
    onNavigationBackEvent: () -> Unit,
    actionsSubscriber: ((A) -> Unit)? = null,
    previewRenderState: S? = null
) {
    skipForPreview(
        previewComposable = {
            previewRenderState?.also { previewState ->
                screenContent.onStateNormal(
                    state = previewState,
                    actions = EmaImmutableActionDispatcherEmpty()
                )
            }

        }
    ) {
        val viewModel = EmaScreenProvider.provideComposableViewModel(androidViewModel = remember {
            vm.invoke()
        })

        val immutableActions = remember {
            viewModel.asActionDispatcher<A>().toImmutable()
        }

        renderScreen(
            initializer,
            screenContent,
            viewModel,
            immutableActions,
            onNavigationEvent,
            onNavigationBackEvent,
            actionsSubscriber
        )
    }
}

@Composable
private fun <A : ViewModelEmaAction, D : EmaNavigationEvent, S : EmaDataState> renderScreen(
    initializer: EmaInitializer?,
    screenContent: EmaComposableScreenContent<S, A>,
    vm: EmaViewModel<S, D>,
    immutableActions: EmaImmutableActionDispatcher<A>,
    onNavigationEvent: (D) -> Unit,
    onNavigationBackEvent: () -> Unit,
    actionSubscriber: ((A) -> Unit)? = null,
    ) {
    vm.onBackHardwarePressedListener?.also {
        BackHandler(true) {
            if (it.invoke())
                onNavigationBackEvent.invoke()
        }
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            val TAG = "LIFECYCLE"
            when (event) {
                Lifecycle.Event.ON_CREATE -> {
                    Log.d(TAG, "On create")
                    vm.onCreated(initializer)
                }

                Lifecycle.Event.ON_START -> {
                    Log.d(TAG, "On start")
                    vm.onStartView()
                }

                Lifecycle.Event.ON_RESUME -> {
                    Log.d(TAG, "On resume")
                    vm.onResumeView()
                }

                Lifecycle.Event.ON_PAUSE -> {
                    Log.d(TAG, "On pause")
                    vm.onPauseView()
                }

                Lifecycle.Event.ON_STOP -> {
                    Log.d(TAG, "On stop")
                    vm.onStopView()
                }

                Lifecycle.Event.ON_DESTROY -> {
                    Log.d(TAG, "On destroy")
                }

                Lifecycle.Event.ON_ANY -> {
                    Log.d(TAG, "On any")
                }
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            vm.onPauseView()
            vm.onStopView()
            lifecycle.removeObserver(observer)
        }
    }

    val state = vm.subscribeStateUpdates()
        .collectAsStateWithLifecycle(initialValue = vm.initialState, lifecycle = lifecycle).value

    LaunchedEffect(key1 = Unit) {
        vm.subscribeToNavigationEvents().collect { event ->
            event.onNavigation { eventData ->
                when (eventData) {
                    is EmaNavigationDirection.Back -> {
                        onNavigationBackEvent.invoke()
                    }

                    is EmaNavigationDirection.Forward -> {
                        onNavigationEvent(eventData.navigationEvent as D)
                    }
                }
                vm.consumeNavigation()
            }
        }
    }

    screenContent.onStateNormal(state.data, immutableActions)
    OverlappedComposable((state as? EmaState.Overlapped), screenContent, immutableActions)


    val event = vm.subscribeToSingleEvents()
        .collectAsStateWithLifecycle(initialValue = EmaExtraData(), lifecycle = lifecycle).value
    val context = LocalContext.current
    LaunchedEffect(key1 = event) {
        if (event is EmaEvent.Launched) {
            screenContent.onSingleEvent(context, event.data, immutableActions)
            vm.consumeSingleEvent()
        }
    }

    actionSubscriber?.also {
        LaunchedEffect(key1 = Unit) {
            vm.asActionDispatcher<A>().subscribeToActions().collect{
                actionSubscriber(it)
            }
        }
    }
}

@Composable
private fun <S : EmaDataState, A : ViewModelEmaAction> OverlappedComposable(
    overlappedState: EmaState.Overlapped<S>? = null,
    screenContent: EmaComposableScreenContent<S, A>,
    actions: EmaImmutableActionDispatcher<A>
) {
    overlappedState?.also {
        screenContent.onStateOverlapped(extra = overlappedState.dataOverlapped, actions = actions)
    }
}