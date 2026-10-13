package com.carmabs.emanate.compose.provider

import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carmabs.emanate.android.viewmodel.EmaAndroidViewModel
import com.carmabs.emanate.android.viewmodel.EmaViewModelFactory
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import com.carmabs.emanate.core.viewmodel.EmaViewModelContract

object EmaScreenProvider {

    @Composable
    fun <S : EmaState, E : EmaEvent> provideComposableViewModel(
        viewModel: EmaViewModelContract<S, E>,
        savedStateHandle: SavedStateHandle?
    ): EmaAndroidViewModel<S, E> = viewModel(
        modelClass = EmaAndroidViewModel::class.java,
        key = viewModel.id,
        factory = EmaViewModelFactory(viewModel, savedStateHandle)
    ) as EmaAndroidViewModel<S, E>
}
