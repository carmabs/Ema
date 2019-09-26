package com.carmabs.ema.presentation.base

import com.carmabs.ema.R
import com.carmabs.ema.android.ui.EmaToolbarFragmentActivity
import com.carmabs.ema.presentation.injection.activityInjection
import org.kodein.di.Kodein

/**
 * TODO: Add a class header comment.
 *
*
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

abstract class BaseActivity : EmaToolbarFragmentActivity() {

    override fun injectActivityModule(kodein: Kodein.MainBuilder): Kodein.Module? = activityInjection(this)

    override fun getNavGraph(): Int = R.navigation.navigation_ema_home
}