package com.carmabs.ema.presentation.ui.splash

import android.view.LayoutInflater
import android.view.ViewGroup
import com.carmabs.ema.android.di.injectDirect
import com.carmabs.ema.android.ui.EmaFragment
import com.carmabs.ema.core.navigator.EmaNavigator
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.sample.ema.databinding.SplashFragmentBinding


class SplashFragment :
    EmaFragment<SplashFragmentBinding,EmaState.EMPTY, SplashViewModel, SplashEvent>() {

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): SplashFragmentBinding {
        return SplashFragmentBinding.inflate(inflater,container,false)
    }



    override fun provideViewModel(): SplashViewModel {
        return injectDirect()
    }

    override fun SplashFragmentBinding.onState(state: EmaState.EMPTY){
    
    }

    override suspend fun SplashFragmentBinding.onEvent(event: SplashEvent) {
        navigate(event)
    }

    override val navigator: EmaNavigator<SplashEvent> = SplashNavigator(this)
}
