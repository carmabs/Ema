package com.carmabs.ema.core.navigator

/**
 * Navigation state to set navigation destination. All clases where [EmaBaseNavigator] can navigate
 * have to implement this interface
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

interface EmaNavigationState {

    fun  navigateWith(navigator: EmaBaseNavigator<out EmaNavigationState>)
}