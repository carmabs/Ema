package com.carmabs.ema.android.delegates

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.carmabs.ema.android.ui.EmaView
import com.carmabs.ema.android.viewmodel.EmaViewModel
import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.ema.core.state.EmaBaseState
import kotlin.reflect.KProperty

/**
 * Created by Carlos Mateo Benito on 13/02/2021.
 *
 * <p>
 * Copyright (c) 2021 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
@Suppress("ClassName")
class emaViewModelDelegate<S : EmaBaseState, NS : EmaNavigationState, VM : EmaViewModel<S, NS>>(
    private val fragmentViewModelScope: Boolean = true
) {

    private var vm: VM? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): VM {
        val emaView = (thisRef as? EmaView<S, VM, NS>) ?: throw IllegalAccessException(
            "You must use this delegate " +
                    "in an object that inherits from EmaView"
        )
        val activity: FragmentActivity = when (thisRef) {
            is Fragment -> thisRef.requireActivity()
            is FragmentActivity -> thisRef
            is View -> thisRef.context as FragmentActivity
            else -> throw IllegalAccessException("The view must be contained inside a FragmentActivity lifecycle")
        }

        val fragment: Fragment? = if (fragmentViewModelScope) emaView as? Fragment else null

        return vm ?: let { emaView.initializeViewModel(activity,fragment).apply {
                vm = this
            }
        }
    }
}