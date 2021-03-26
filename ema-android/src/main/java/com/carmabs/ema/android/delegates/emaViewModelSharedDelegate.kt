package com.carmabs.ema.android.delegates

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.carmabs.ema.android.ui.EmaAndroidView
import com.carmabs.ema.android.ui.EmaFragment
import com.carmabs.ema.android.viewmodel.EmaAndroidViewModel
import com.carmabs.ema.core.state.EmaBaseState
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.viewmodel.EmaViewModel
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
class emaViewModelSharedDelegate<S : EmaBaseState, VM : EmaAndroidViewModel<out EmaViewModel<S, *>>>(
    private val observerFunction: ((attachedState: EmaState<S>) -> Unit)? = null,
    private val viewModelSeed: () -> VM

) {

    private var vm: VM? = null


    operator fun getValue(thisRef: Any?, property: KProperty<*>): VM {
        val emaView = (thisRef as? EmaAndroidView<*, *, *>)
            ?: throw IllegalAccessException(
                "You must use this delegate " +
                        "in an object that inherits from EmaView"
            )
        val activity: FragmentActivity = when (thisRef) {
            is Fragment -> thisRef.requireActivity()
            is FragmentActivity -> thisRef
            is View -> thisRef.context as FragmentActivity
            else -> throw IllegalAccessException("The view must be contained inside a FragmentActivity lifecycle")
        }

        return vm ?: (when (emaView) {
            is EmaFragment -> {
                emaView.addExtraViewModel(
                    viewModelSeed.invoke(),
                    emaView,
                    activity,
                    observerFunction
                ) as VM
            }
            else -> throw IllegalAccessException("The view must be a EmaFragment")
        }).apply {
            vm = this
        }
    }
}