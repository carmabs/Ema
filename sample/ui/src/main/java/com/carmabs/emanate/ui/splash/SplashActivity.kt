package com.carmabs.emanate.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.carmabs.emanate.android.navigation.EmaActivityNavControllerHost
import com.carmabs.emanate.android.ui.EmaToolbarActivity
import com.carmabs.emanate.core.navigator.EmaNavigator
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import com.carmabs.emanate.core.viewmodel.EmaViewModel
import com.carmabs.emanate.sample.ui.R
import com.carmabs.emanate.sample.ui.databinding.SplashActivityBinding
import com.google.android.material.appbar.AppBarLayout

class SplashActivity :
    EmaToolbarActivity<SplashActivityBinding, EmaState.EMPTY, EmaViewModel.EMPTY, EmaEvent.EMPTY>() {

    override fun createViewBinding(inflater: LayoutInflater): SplashActivityBinding =
        SplashActivityBinding.inflate(inflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        hideToolbar(animate = false)
        applyWindowInsets()
    }

    override fun SplashActivityBinding.provideToolbar(): Toolbar = tbSplash

    override fun SplashActivityBinding.provideToolbarLayout(): AppBarLayout = ablSplash

    override fun provideViewModel(): EmaViewModel.EMPTY = EmaViewModel.EMPTY

    override fun SplashActivityBinding.onState(data: EmaState.EMPTY) {
    }

    override val navigator: EmaNavigator<EmaEvent.EMPTY> = EmaActivityNavControllerHost(
        this,
        R.id.navHostFragment,
        R.navigation.main_graph
    )

    /**
     * The app is drawn edge to edge, so the fragments container is padded with the system bars
     * and the keyboard insets
     */
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.navHostFragment) { view, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            view.updatePadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }
}
