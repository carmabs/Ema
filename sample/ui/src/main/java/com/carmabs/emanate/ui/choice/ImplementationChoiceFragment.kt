package com.carmabs.emanate.ui.choice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.carmabs.emanate.android.ui.EmaFragment
import com.carmabs.emanate.core.navigator.EmaNavigator
import com.carmabs.emanate.core.savestate.EmaStateStore
import com.carmabs.emanate.core.state.EmaState
import com.carmabs.emanate.presentation.choice.ImplementationChoiceAction
import com.carmabs.emanate.presentation.choice.ImplementationChoiceEvent
import com.carmabs.emanate.presentation.choice.ImplementationChoiceViewModel
import com.carmabs.emanate.presentation.choice.ViewModelImplementation
import com.carmabs.emanate.sample.ui.databinding.ImplementationChoiceFragmentBinding
import org.koin.android.ext.android.get

/**
 * First screen of the sample: it explains the two implementations of the view models and lets choose the one used
 * by the rest of screens
 */
class ImplementationChoiceFragment :
    EmaFragment<
        ImplementationChoiceFragmentBinding,
        EmaState.EMPTY,
        ImplementationChoiceViewModel,
        ImplementationChoiceEvent
        >() {

    override fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ImplementationChoiceFragmentBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cvChoiceEmaViewModel.setOnClickListener {
            select(ViewModelImplementation.EMA_VIEW_MODEL)
        }
        binding.cvChoiceEmaViewModelAction.setOnClickListener {
            select(ViewModelImplementation.EMA_VIEW_MODEL_ACTION)
        }
    }

    private fun select(implementation: ViewModelImplementation) {
        viewModel.dispatch(ImplementationChoiceAction.ImplementationSelected(implementation))
    }

    override fun ImplementationChoiceFragmentBinding.onState(state: EmaState.EMPTY) = Unit

    override fun provideViewModel(store: EmaStateStore): ImplementationChoiceViewModel = get()

    override suspend fun ImplementationChoiceFragmentBinding.onEvent(event: ImplementationChoiceEvent) {
        navigate(event)
    }

    override val navigator: EmaNavigator<ImplementationChoiceEvent> = ImplementationChoiceNavigator(this)
}
