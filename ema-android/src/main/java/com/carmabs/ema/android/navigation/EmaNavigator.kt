package com.carmabs.ema.android.navigation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import com.carmabs.ema.android.ui.EmaActivity
import com.carmabs.ema.core.navigator.EmaBaseNavigator
import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.ema.core.state.EmaBaseState

/**
 *  *<p>
 * Copyright (c) 2020, Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 *
 * Navigator to handle navigation through navController
 * Created by: Carlos Mateo Benito on 20/1/19.
 */
interface EmaNavigator<NS : EmaNavigationState> : EmaBaseNavigator<NS> {

    val navController: NavController

    /**
     * Set the initial state for the incoming fragment. Only work for fragment at the same activity
     * at the moment
     * @param emaBaseState the state for the incoming view
     * @param inputStateKey the key to identify the state. If it not provided, it will take the canonical name
     * of the state
     */
    fun addInputState(emaBaseState: EmaBaseState, inputStateKey: String? = null): Bundle =
            Bundle().apply {
                putSerializable(inputStateKey ?: emaBaseState.javaClass.name, emaBaseState)
            }

    /**
     * Navigate with android architecture components within action ID
     * @param actionID
     * @param data
     * @param navOptions
     */
    fun navigateWithAction(@IdRes actionID: Int, data: Bundle? = null, navOptions: NavOptions? = null) {
        navController.navigate(actionID, data, navOptions)
    }


    /**
     * Navigate to new activity with result handling
     * @param mainActivity activity will launch the next one
     * @param destinationActivity new destination activity class
     * @param data the data set up for next view, use addInputState
     * @param requestCode code for result
     * @param finishMain if [mainActivity] must be finished when [destinationActivity] is launched
     */
    fun navigateToEmaActivityWithResult(mainEmaActivity: Activity, destinationEmaActivity: Intent, finishMain: Boolean = false) {
        (mainEmaActivity as? EmaActivity<*,*,*>)?.also {
            it.startActivityForResult(destinationEmaActivity, EmaActivity.RESULT_DEFAULT_CODE)
            if (finishMain) {
                it.finish()
            }
        }?:throw ClassCastException("The launcher activity and the destination activity must" +
                "extend from EmaActivity")
    }

    /**
     * Navigates back
     * @return true if a fragment has been popped, false if backstack is empty, in that case, finish
     * the activity provided.
     */
    fun navigateBack(activity: Activity): Boolean {
        val hasMoreFragments = navController.popBackStack()
        if(!hasMoreFragments)
            activity.finish()

        return hasMoreFragments
    }

    /**
     * Navigate with android architecture components within navDirections safeargs
     * @param navDirections
     * @param navOptions
     */
    fun navigateWithDirections(navDirections: NavDirections, navOptions: NavOptions? = null) {
        navController.navigate(navDirections, navOptions)
    }


    /**
     * Navigates back
     * @return true if a fragment has been popped, false if backstack is empty
     */
    override fun navigateBack(): Boolean {
        return navController.popBackStack()
    }
}