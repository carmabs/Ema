package com.carmabs.emanate.presentation.choice

import com.carmabs.emanate.core.state.EmaEvent

sealed interface ImplementationChoiceEvent : EmaEvent {

    data object ImplementationChosen : ImplementationChoiceEvent
}
