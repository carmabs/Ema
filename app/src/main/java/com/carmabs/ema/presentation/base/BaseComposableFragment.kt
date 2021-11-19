package com.carmabs.ema.presentation.base

import androidx.compose.runtime.Composable
import com.carmabs.ema.android.ui.EmaFragment
import com.carmabs.ema.android.uicompose.EmaComposableFragment
import com.carmabs.ema.core.navigator.EmaNavigationState
import com.carmabs.ema.core.state.EmaBaseState
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.core.viewmodel.EmaViewModel
import com.carmabs.ema.presentation.injection.fragmentInjection
import org.kodein.di.DI

/**
 *  *<p>
 * Copyright (c) 2020, Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

abstract class BaseComposableFragment<S : EmaBaseState, VM : EmaViewModel<S, NS>, NS : EmaNavigationState> :
    EmaComposableFragment<S, VM, NS>() {

    override val fragmentViewModelScope: Boolean = true

    override fun injectFragmentModule(kodein: DI.MainBuilder): DI.Module? = fragmentInjection(this)

    @Composable
    override fun onStateNormalComposable(data: S) {
        onNormal(data)
    }

    @Composable
    override fun onStateAlternativeComposable(data: EmaExtraData) {
        onAlternative(data)
    }

    @Composable
    override fun onSingleEventComposable(data: EmaExtraData) {
        onSingle(data)
    }

    @Composable
    override fun onStateErrorComposable(error: Throwable) {
        onError(error)
    }

    @Composable
    abstract fun onNormal(data: S)

    @Composable
    abstract fun onAlternative(data: EmaExtraData)

    @Composable
    abstract fun onSingle(data: EmaExtraData)

    @Composable
    abstract fun onError(error: Throwable)

}