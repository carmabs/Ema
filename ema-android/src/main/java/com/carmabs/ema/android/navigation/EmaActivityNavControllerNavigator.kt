package com.carmabs.ema.android.navigation

import android.app.Activity
import androidx.annotation.IdRes
import androidx.annotation.NavigationRes
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.carmabs.ema.core.navigator.EmaNavigationTarget
import com.carmabs.ema.core.state.EmaBaseState

/**
 * Created by Carlos Mateo Benito on 29/7/22.
 *
 * <p>
 * Copyright (c) 2022 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 *
 * Navigator to handle navigation through navController in an Activity
 * Created by: Carlos Mateo Benito on 29/07/22.
 */
abstract class EmaActivityNavControllerNavigator<NS : EmaNavigationTarget>(
    override val activity: Activity,
    @IdRes private val navHostId: Int,
    @NavigationRes private val graphId: Int
) : EmaNavControllerNavigator<NS> {

    fun setup(state: EmaBaseState?) {
        navController.setGraph(graphId, state?.let {
            addInputState(it)
        } ?: activity.intent.extras)
    }

    override val navController: NavController
        get() = activity.findNavController(navHostId)
}