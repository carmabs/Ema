package com.carmabs.ema.presentation.ui.home

import com.carmabs.ema.R
import com.carmabs.ema.android.ui.EmaActivity
import com.carmabs.ema.android.ui.EmaFragmentActivity
import com.carmabs.ema.core.navigator.EmaBaseNavigator
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.presentation.base.BaseActivity
import com.carmabs.ema.presentation.injection.activityInjection
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

/**
 * Project: Ema
 * Created by: cmateob on 20/1/19.
 */
class EmaHomeActivity : BaseActivity<EmaHomeToolbarState,EmaHomeToolbarViewModel,EmaHomeNavigator.Navigation>() {

    override fun getNavGraph(): Int = R.navigation.navigation_ema_home

    override val inputStateKey: String? = null

    override fun onInitialized(viewModel: EmaHomeToolbarViewModel) {

    }

    override fun getToolbarTitle(): String? {
        return "HOLA"
    }

    override val viewModelSeed: EmaHomeToolbarViewModel by instance()

    override val navigator: EmaHomeNavigator by instance()

    override fun onStateNormal(data: EmaHomeToolbarState) {

    }

    override fun onStateLoading(data: EmaExtraData) {

    }

    override fun onSingleEvent(data: EmaExtraData) {

    }

    override fun onStateError(error: Throwable) {

    }
}