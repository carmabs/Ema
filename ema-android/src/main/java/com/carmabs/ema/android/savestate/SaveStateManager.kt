package com.carmabs.ema.android.savestate

import androidx.lifecycle.SavedStateHandle
import com.carmabs.ema.core.state.EmaEvent
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.viewmodel.EmaViewModel
import kotlinx.coroutines.CoroutineScope

/**
 * Created by Carlos Mateo Benito on 22/2/24.
 *
 * <p>
 * Copyright (c) 2024 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.comm”>Carlos Mateo Benito</a>
 */
interface SaveStateManager<S : EmaState, E : EmaEvent> {
    fun onSaveStateHandling(
        scope: CoroutineScope,
        saveStateHandle: SavedStateHandle,
        viewModel: EmaViewModel<S, E>
    )
}

fun <S : EmaState, E : EmaEvent> emaSaveStateManager(
    scope: CoroutineScope,
    saveStateHandle: SavedStateHandle,
    viewModel: EmaViewModel<S, E>
) {

}
