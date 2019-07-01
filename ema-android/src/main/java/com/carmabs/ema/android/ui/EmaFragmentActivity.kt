package com.carmabs.ema.android.ui

import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.carmabs.ema.android.R

/**
 * Abstract class to handle navigation in activity
 *
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
abstract class EmaFragmentActivity : EmaBaseActivity() {

    override fun createActivity(savedInstanceState: Bundle?) {
        setupNavigation()
    }

    /**
     * Get the nav controller to handle the navigation through navigation architecture components.
     * The nav controller must be provided by an id called "navHostFragment"
     */
    override fun getNavController(): NavController {

        try {
            return findNavController(R.id.navHostFragment)
        } catch (e: java.lang.RuntimeException) {
            throw RuntimeException("You must provide in your activity xml a fragment with " +
                    "android:id=@+ìd/navHostFragment as container " +
                    "with android:name=androidx.navigation.fragment.NavHostFragment")
        }

    }
    /**
     * Setup the navigation path for navigation architecture components
     */
    private fun setupNavigation() {
        navController.setGraph(getNavGraph())
    }


    /**
     * Get the navigation resource for the activity [R.navigation]
     */
    abstract fun getNavGraph(): Int


    /**
     * Set up the up action navigation
     */
    override fun onSupportNavigateUp() =
            findNavController(R.id.navHostFragment).navigateUp()

    /**
     * Get the view whic acts as fragment container
     */
    protected fun getContentLayout(): View {
        return findViewById(R.id.navHostFragment)
    }

    /**
     * The layout id for the activity
     */
    override fun getLayout(): Int {
        return R.layout.ema_activity_fragment
    }
}