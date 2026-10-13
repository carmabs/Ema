package com.carmabs.emanate.presentation.choice

import com.carmabs.emanate.core.action.EmaAction

sealed interface ImplementationChoiceAction : EmaAction {

    data class ImplementationSelected(val implementation: ViewModelImplementation) : ImplementationChoiceAction
}
