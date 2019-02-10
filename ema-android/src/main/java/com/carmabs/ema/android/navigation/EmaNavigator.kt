package com.carmabs.ema.android.navigation

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import com.carmabs.ema.core.navigator.EmaBaseNavigator
import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.ema.core.state.EmaBaseState

/**
 * Project: Ema
 * Navigator to handle navigation through navController
 * Created by: cmateob on 20/1/19.
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
    fun addInputState(emaBaseState: EmaBaseState,inputStateKey: String?=null) : Bundle =
            Bundle().apply { putSerializable(inputStateKey?:emaBaseState.javaClass.name, emaBaseState)
    }

    /**
     * Navigate with android architecture components within action ID
     * @param actionID
     * @param data
     * @param navOptions
     */
    fun navigateWithAction(@IdRes actionID:Int,data:Bundle?=null,navOptions: NavOptions?=null){
        navController.navigate(actionID,data,navOptions)
    }

    /**
     * Navigate with android architecture components within navDirections safeargs
     * @param navDirections
     * @param navOptions
     */
    fun navigateWithDirections(navDirections: NavDirections,navOptions: NavOptions?=null){
        navController.navigate(navDirections,navOptions)
    }

    /**
     * Navigates back
     */
    fun navigateBack(){
        navController.popBackStack()
    }
}