package com.carmabs.ema.presentation.base

import com.carmabs.ema.android.ui.EmaActivity
import com.carmabs.ema.android.viewmodel.EmaViewModel
import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.ema.core.state.EmaBaseState
import com.carmabs.ema.presentation.injection.activityInjection
import org.kodein.di.Kodein

/**
 * TODO: Add a class header comment.
 *
*
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

abstract class BaseActivity<S : EmaBaseState, VM : EmaViewModel<S, NS>, NS : EmaNavigationState> : EmaActivity<S,VM,NS>() {

    override fun injectActivityModule(kodein: Kodein.MainBuilder): Kodein.Module? = activityInjection(this)
}