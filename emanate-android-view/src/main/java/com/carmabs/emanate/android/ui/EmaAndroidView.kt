package com.carmabs.emanate.android.ui

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.carmabs.emanate.android.viewmodel.EmaAndroidViewModel
import com.carmabs.emanate.android.viewmodel.EmaViewModelFactory
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import com.carmabs.emanate.core.view.EmaView
import com.carmabs.emanate.core.viewmodel.EmaViewModelContract

/**
 * View to handle VM view logic states through [EmaState].
 * The user must provide in the constructor by template:
 *  - The view model class [EmaViewModelContract] is going to use the view
 *  - The navigation state class [EmaNavigationEvent] will handle the navigation
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
interface EmaAndroidView<S : EmaState, VM : EmaViewModelContract<S, E>, E : EmaEvent> : EmaView<S, VM, E> {

    fun initializeViewModel(activity: ComponentActivity, viewModelSeed: VM): EmaAndroidViewModel<S, E> {
        val emaFactory = EmaViewModelFactory(viewModelSeed)
        val vm = ViewModelProvider(
            activity,
            emaFactory
        )[viewModelSeed.id, EmaAndroidViewModel::class.java]
        return vm as EmaAndroidViewModel<S, E>
    }

    fun initializeViewModel(fragment: Fragment, viewModelSeed: VM): EmaAndroidViewModel<S, E> {
        val emaFactory = EmaViewModelFactory(viewModelSeed)
        val vm = ViewModelProvider(
            fragment,
            emaFactory
        )[viewModelSeed.id, EmaAndroidViewModel::class.java]
        return vm as EmaAndroidViewModel<S, E>
    }
}
