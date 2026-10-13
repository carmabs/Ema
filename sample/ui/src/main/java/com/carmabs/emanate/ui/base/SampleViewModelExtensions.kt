package com.carmabs.emanate.ui.base

import android.content.ComponentCallbacks
import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import com.carmabs.emanate.presentation.base.SampleViewModel
import com.carmabs.emanate.presentation.choice.ViewModelImplementation
import com.carmabs.emanate.presentation.choice.ViewModelImplementationSelector
import kotlin.reflect.KClass
import org.koin.android.ext.android.get
import org.koin.android.ext.android.getKoin

/**
 * Creates the view model of a screen with the implementation chosen when the sample starts.
 *
 * @param viewModel built with EmaViewModel
 * @param actionViewModel built with EmaViewModelAction
 */
fun <S : EmaState, A : EmaAction, E : EmaEvent> ComponentCallbacks.sampleViewModel(
    viewModel: KClass<out SampleViewModel<S, A, E>>,
    actionViewModel: KClass<out SampleViewModel<S, A, E>>
): SampleViewModel<S, A, E> {
    val implementation = when (get<ViewModelImplementationSelector>().implementation) {
        ViewModelImplementation.EMA_VIEW_MODEL -> viewModel
        ViewModelImplementation.EMA_VIEW_MODEL_ACTION -> actionViewModel
    }
    return getKoin().get(implementation)
}
