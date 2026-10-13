package com.carmabs.emanate.ui.choice

import androidx.fragment.app.Fragment
import com.carmabs.emanate.android.navigation.EmaFragmentNavControllerNavigator
import com.carmabs.emanate.presentation.choice.ImplementationChoiceEvent
import com.carmabs.emanate.sample.ui.R

class ImplementationChoiceNavigator(fragment: Fragment) :
    EmaFragmentNavControllerNavigator<ImplementationChoiceEvent>(fragment) {

    override fun navigate(event: ImplementationChoiceEvent) {
        when (event) {
            ImplementationChoiceEvent.ImplementationChosen ->
                navController.navigate(R.id.action_implementationChoiceFragment_to_splashFragment)
        }
    }
}
