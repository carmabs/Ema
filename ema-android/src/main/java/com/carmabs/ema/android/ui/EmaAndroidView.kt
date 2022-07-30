package com.carmabs.ema.android.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.carmabs.ema.android.viewmodel.EmaAndroidViewModel
import com.carmabs.ema.android.viewmodel.EmaFactory
import com.carmabs.ema.core.navigator.EmaNavigationTarget
import com.carmabs.ema.core.state.EmaBaseState
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.view.EmaView
import com.carmabs.ema.core.viewmodel.EmaViewModel


/**
 * View to handle VM view logic states through [EmaState].
 * The user must provide in the constructor by template:
 *  - The view model class [EmaViewModel] is going to use the view
 *  - The navigation state class [EmaNavigationTarget] will handle the navigation
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
interface EmaAndroidView<S : EmaBaseState, VM : EmaViewModel<S, NS>, NS : EmaNavigationTarget> :
    EmaView<S, VM, NS> {

    val androidViewModelSeed: EmaAndroidViewModel<VM>

    fun initializeViewModel(
        fragmentActivity: FragmentActivity,
    ): VM {
        val emaFactory = EmaFactory(androidViewModelSeed)
        val vm = ViewModelProvider(
            fragmentActivity,
            emaFactory
        )[androidViewModelSeed::class.java]


        return vm.emaViewModel as VM
    }

    fun initializeViewModel(
        fragment: Fragment
    ): VM {
        val emaFactory = EmaFactory(androidViewModelSeed)
        val vm = ViewModelProvider(fragment, emaFactory)[androidViewModelSeed::class.java]
        return vm.emaViewModel as VM
    }
}