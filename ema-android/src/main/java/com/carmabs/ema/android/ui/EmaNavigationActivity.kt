package com.carmabs.ema.android.ui

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.viewbinding.ViewBinding
import com.carmabs.ema.android.navigation.EmaActivityNavControllerNavigator
import com.carmabs.ema.android.navigation.EmaNavControllerNavigator
import com.carmabs.ema.core.initializer.EmaInitializer
import com.carmabs.ema.core.navigator.EmaNavigationTarget
import com.carmabs.ema.core.navigator.EmaNavigator
import com.carmabs.ema.core.state.EmaBaseState

/**
 * Abstract class to handle navigation in activity
 *
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
abstract class EmaNavigationActivity<B : ViewBinding, NT : EmaNavigationTarget> :
    EmaBaseActivity<B>() {

    abstract val navigator: EmaNavigator<NT>?

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (navigator as? EmaActivityNavControllerNavigator)?.setup(overrideDestinationInitializer())
    }

    /**
     * Set up the up action navigation
     */
    override fun onSupportNavigateUp() =
        (navigator as? EmaNavControllerNavigator)?.let { it.navController.navigateUp() } ?: false

    protected open fun overrideDestinationInitializer(): EmaInitializer? = null
}