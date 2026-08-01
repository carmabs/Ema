package com.carmabs.ema.compose.provider

import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carmabs.ema.android.viewmodel.EmaAndroidViewModel
import com.carmabs.ema.android.viewmodel.EmaViewModelFactory
import com.carmabs.ema.core.state.EmaEvent
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.viewmodel.EmaViewModel

object EmaScreenProvider {

    @Composable
    fun <S : EmaState, E : EmaEvent> provideComposableViewModel(
        viewModel: EmaViewModel<S, E>,
        savedStateHandle: SavedStateHandle?
    ): EmaAndroidViewModel<S, E> {
        return viewModel(
            modelClass = EmaAndroidViewModel::class.java,
            key = viewModel.id,
            factory = EmaViewModelFactory(viewModel, savedStateHandle)
        ) as EmaAndroidViewModel<S, E>
    }
}
