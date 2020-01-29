package com.carmabs.ema.presentation.ui.backdata;

import androidx.core.content.ContextCompat
import com.carmabs.ema.R
import com.carmabs.ema.android.ui.EmaActivity
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.presentation.base.BaseActivity
import com.carmabs.ema.presentation.injection.activityInjection
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

/**
 *<p>
 * Copyright (c) 2020, Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 * <p>
 * Date: 2019-11-07
 */

class EmaBackToolbarActivity : BaseActivity<EmaBackToolbarState, EmaBackToolbarViewModel, EmaBackNavigator.Navigation>() {

    override val navGraph: Int = R.navigation.navigation_ema_back

    override fun provideFixedToolbarTitle(): String? = null

    override val viewModelSeed: EmaBackToolbarViewModel by instance()

    override val navigator: EmaBackNavigator by instance()

    override fun onInitialized(viewModel: EmaBackToolbarViewModel) {
        toolbar.apply {
            setBackgroundColor(ContextCompat.getColor(this@EmaBackToolbarActivity, R.color.colorPrimary))
        }

    }

    override fun onStateNormal(data: EmaBackToolbarState) {

    }

    override fun onStateAlternative(data: EmaExtraData) {

    }

    override fun onSingleEvent(data: EmaExtraData) {

    }

    override fun onStateError(error: Throwable) {

    }
}