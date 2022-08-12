package com.carmabs.ema.android.ui

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.carmabs.ema.android.viewmodel.EmaAndroidViewModel
import com.carmabs.ema.android.viewmodel.EmaViewModelFactory
import com.carmabs.ema.core.navigator.EmaDestination
import com.carmabs.ema.core.state.EmaBaseState
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.view.EmaView
import com.carmabs.ema.core.viewmodel.EmaViewModel


/**
 * View to handle VM view logic states through [EmaState].
 * The user must provide in the constructor by template:
 *  - The view model class [EmaViewModel] is going to use the view
 *  - The navigation state class [EmaDestination] will handle the navigation
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
interface EmaAndroidView<S : EmaBaseState, VM : EmaViewModel<S, D>, D : EmaDestination> :
    EmaView<S, VM, D> {

    val androidViewModelSeed: EmaAndroidViewModel<VM>

    fun initializeViewModel(
        activity: ComponentActivity,
    ): VM {
        val emaFactory = EmaViewModelFactory(androidViewModelSeed)
        val vm = ViewModelProvider(
            activity,
            emaFactory
        )[androidViewModelSeed::class.java]


        return vm.emaViewModel as VM
    }

    fun initializeViewModel(
        fragment: Fragment
    ): VM {
        val emaFactory = EmaViewModelFactory(androidViewModelSeed)
        val vm = ViewModelProvider(fragment, emaFactory)[androidViewModelSeed::class.java]
        return vm.emaViewModel as VM
    }
}