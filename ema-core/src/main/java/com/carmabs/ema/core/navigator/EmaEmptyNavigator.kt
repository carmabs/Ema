package com.carmabs.ema.core.navigator

/**
 * EmaBaseNavigator used to navigate between views. You have to provide a NavigationState object
 * where you must handle the navigation implementation. A good approach is to define a sealed class
 * inside the EmaBaseNavigator implementation class  with all the possibles destination
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

class EmaEmptyNavigator : EmaNavigator<EmaNavigationState> {

    class EmaEmptyNavigationState : EmaNavigationState {
        override fun navigateWith(navigator: EmaNavigator<out EmaNavigationState>) {
            //DO NOTHING
        }
    }

    override fun navigateBack(): Boolean {
        return true
    }
}