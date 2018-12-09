package com.carmabs.ema.core.navigator

/**
 * Navigation state to set navigation destination. All clases where [EmaNavigator] can navigate
 * have to implement this interface
 *
 * @author <a href=“mailto:carlos.mateo@babel.es”>Carlos Mateo</a>
 */

interface EmaNavigationState {

    fun  navigateWith(navigator: EmaNavigator<out EmaNavigationState>)
}