package com.carmabs.ema.core.navigator

/**
 * EmaBaseNavigator used to navigate between views. You have to provide a NavigationState object
 * where you must handle the navigation implementation. A good approach is to define a sealed class
 * inside the EmaBaseNavigator implementation class  with all the possibles destination
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

interface EmaBaseNavigator<T : EmaNavigationState> {

    fun navigate(navigationState: T){
        navigationState.navigateWith(this)
    }

    fun navigateBack():Boolean
}