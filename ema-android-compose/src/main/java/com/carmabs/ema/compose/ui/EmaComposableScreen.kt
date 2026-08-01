package com.carmabs.ema.compose.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.carmabs.ema.android.savestate.SavedStateSupport
import com.carmabs.ema.compose.action.EmaImmutableActionDispatcher
import com.carmabs.ema.compose.action.EmaImmutableActionDispatcherEmpty
import com.carmabs.ema.compose.action.toImmutable
import com.carmabs.ema.compose.extension.asActionDispatcher
import com.carmabs.ema.compose.extension.skipForPreview
import com.carmabs.ema.compose.provider.EmaScreenProvider
import com.carmabs.ema.core.action.EmaAction
import com.carmabs.ema.core.action.EmaActionDispatcher
import com.carmabs.ema.core.action.EmaEventDispatcher
import com.carmabs.ema.core.initializer.EmaInitializer
import com.carmabs.ema.core.model.EmaBackHandlerStrategy
import com.carmabs.ema.core.state.EmaEvent
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.viewmodel.EmaViewModel

@Composable
fun <S : EmaState, A : EmaAction.Screen, E : EmaEvent> EmaComposableScreen(
    initializer: EmaInitializer? = null,
    vm: EmaViewModel<S, E>,
    actions: EmaActionDispatcher<A>,
    screenContent: EmaComposableScreenContent<S, A, E>,
    onEvent: (E) -> Unit,
    previewRenderState: S? = null
) {
    skipForPreview(
        previewComposable = {
            previewRenderState?.also { previewState ->
                screenContent.onState(
                    state = previewState,
                    actions = EmaImmutableActionDispatcherEmpty()
                )
            }
        }
    ) {

        val immutableActions = remember {
            actions.toImmutable()
        }

        RenderScreen(
            initializer,
            screenContent,
            vm,
            immutableActions,
            onEvent
        )
    }
}

@Composable
fun <A : EmaAction.Screen, S : EmaState, E : EmaEvent> EmaComposableScreen(
    initializer: EmaInitializer? = null,
    vm: () -> EmaViewModel<S, E>,
    screenContent: EmaComposableScreenContent<S, A, E>,
    onEffect: (E) -> Unit,
    saveStateSupport: SavedStateSupport<S, E>? = null,
    previewRenderState: S? = null
) {
    skipForPreview(
        previewComposable = {
            previewRenderState?.also { previewState ->
                screenContent.onState(
                    state = previewState,
                    actions = EmaImmutableActionDispatcherEmpty()
                )
            }

        }
    ) {
        val androidVm = EmaScreenProvider.provideComposableViewModel(viewModel = remember {
            vm.invoke()
        }, saveStateSupport?.savedStateHandle)

        val emaVm = androidVm.emaViewModel

        val immutableActions = remember {
            emaVm.asActionDispatcher<A>().toImmutable()
        }

        saveStateSupport?.saveStateManager?.onSaveStateHandling(
            androidVm.viewModelScope,
            androidVm.savedStateHandle,
            androidVm.emaViewModel
        )

        RenderScreen(
            initializer,
            screenContent,
            emaVm,
            immutableActions,
            onEffect
        )
    }
}

@Composable
private fun <S : EmaState, A : EmaAction.Screen, E : EmaEvent> RenderScreen(
    initializer: EmaInitializer?,
    screenContent: EmaComposableScreenContent<S, A, E>,
    vm: EmaViewModel<S, E>,
    immutableActions: EmaImmutableActionDispatcher<A>,
    onEffect: (E) -> Unit
) {
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

    val state = vm.stateFlow
        .collectAsStateWithLifecycle(initialValue = vm.initialState, lifecycle = lifecycle).value

    val effectFlow = vm.eventFlow
    val context = LocalContext.current
    LaunchedEffect(key1 = effectFlow, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            effectFlow.collect { effects ->
                effects.forEach { effect ->
                    onEffect(effect)
                    screenContent.onEffect(context, effect, immutableActions)
                    (vm as? EmaEventDispatcher<E>)?.consumeEvent(effect)
                }
            }
        }
    }

    if (vm.shouldRenderState) {
        screenContent.onState(state, immutableActions)
    }
}


