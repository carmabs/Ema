package com.carmabs.emanate.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.carmabs.emanate.android.ui.EmaFragment
import com.carmabs.emanate.core.navigator.EmaNavigator
import com.carmabs.emanate.core.state.EmaState
import com.carmabs.emanate.presentation.splash.SplashEvent
import com.carmabs.emanate.presentation.splash.SplashViewModel
import com.carmabs.emanate.sample.ui.databinding.SplashFragmentBinding
import org.koin.android.ext.android.get

class SplashFragment : EmaFragment<SplashFragmentBinding, EmaState.EMPTY, SplashViewModel, SplashEvent>() {

    override fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?): SplashFragmentBinding =
        SplashFragmentBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.animateEntrance()
    }

    private fun SplashFragmentBinding.animateEntrance() {
        llSplashContent.apply {
            alpha = 0f
            translationY = resources.displayMetrics.density * 24
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .start()
        }
    }

    override fun provideViewModel(): SplashViewModel = get()

    override fun SplashFragmentBinding.onState(state: EmaState.EMPTY) {
    }

    override suspend fun SplashFragmentBinding.onEvent(event: SplashEvent) {
        navigate(event)
    }

    override val navigator: EmaNavigator<SplashEvent> = SplashNavigator(this)
}
