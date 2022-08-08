package com.carmabs.ema.core.viewmodel

import com.carmabs.ema.core.navigator.EmaEmptyNavigationTarget
import com.carmabs.ema.core.navigator.EmaNavigationTarget
import com.carmabs.ema.core.state.EmaBaseState
import com.carmabs.ema.core.state.EmaEmptyState

/**
 * Created by Carlos Mateo Benito on 21/7/22.
 *
 * <p>
 * Copyright (c) 2022 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
class EmaEmptyViewModel : EmaViewModel<EmaEmptyState, EmaEmptyNavigationTarget>() {

    override val updateOnInitialization: Boolean
        get() = false

    override fun onStartFirstTime(statePreloaded: Boolean) {
        //DO NOTHING
    }

    override val initialViewState: EmaEmptyState
        get() = EmaEmptyState()

}