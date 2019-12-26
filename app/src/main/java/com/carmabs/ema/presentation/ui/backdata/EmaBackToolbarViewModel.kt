package com.carmabs.ema.presentation.ui.backdata

import com.carmabs.ema.android.viewmodel.EmaViewModel
import com.carmabs.ema.presentation.ui.backdata.creation.EmaBackUserCreationViewModel

/**
 * <p>
 * Copyright (c) 2019, Babel Sistemas de Información. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:carlos.mateo@babel.es”>Carlos Mateo</a>
 *
 * Date: 2019-11-07
 */

class EmaBackToolbarViewModel : EmaViewModel<EmaBackToolbarState, EmaBackNavigator.Navigation>() {

    override fun createInitialViewState(): EmaBackToolbarState {
        return EmaBackToolbarState()
    }

    override fun onStartFirstTime(statePreloaded: Boolean) {

    }

    override fun onResultListenerSetup() {
        addOnResultReceived(EmaBackUserCreationViewModel.RESULT_USER) {
            val p = it
        }
    }
}