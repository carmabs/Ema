package com.carmabs.ema.core.navigator

/**
 * EmaNavigator used to navigate between views. You have to provide a NavigationState object
 * where you must handle the navigation implementation. A good approach is to define a sealed class
 * inside the EmaNavigator implementation class  with all the possibles destination
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

interface EmaNavigator<T : EmaNavigationState> {

    fun navigate(navigationState: T)
}