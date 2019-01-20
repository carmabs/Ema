package com.carmabs.ema.android.navigation

import androidx.navigation.NavHost
import com.carmabs.ema.core.navigator.EmaBaseNavigator
import com.carmabs.ema.core.navigator.EmaNavigationState

/**
 * Project: Ema
 * Created by: cmateob on 20/1/19.
 */
interface EmaNavigator<NS : EmaNavigationState> : EmaBaseNavigator<NS> {
    val navHost: NavHost
}