package ${packageName}

import androidx.navigation.NavController
import com.carmabs.emanate.android.navigation.EmaNavigator
import com.carmabs.emanate.core.navigator.EmaBaseNavigator
import com.carmabs.emanate.core.navigator.EmaNavigationState

class ${functionalityName}Navigator(override val navController: NavController) : EmaNavigator<${functionalityName}Navigator.Navigation> {

    sealed class Navigation : EmaNavigationState {

        object Destination : ${functionalityName}Navigator.Navigation() {
            override fun navigateWith(navigator: EmaBaseNavigator<out EmaNavigationState>) {
              (navigator as? ${functionalityName}Navigator)?.toDestination()
            }
        }
    }

    fun toDestination() {
        navigateWithAction(R.id.navigationAction)
    }
}