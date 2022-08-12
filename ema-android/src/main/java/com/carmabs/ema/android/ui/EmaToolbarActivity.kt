package com.carmabs.ema.android.ui

import android.animation.Animator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.CallSuper
import androidx.appcompat.widget.Toolbar
import androidx.navigation.ui.setupActionBarWithNavController
import com.carmabs.ema.android.R
import com.carmabs.ema.android.databinding.EmaToolbarActivityBinding
import com.carmabs.ema.android.extension.checkVisibility
import com.carmabs.ema.core.constants.FLOAT_ONE
import com.carmabs.ema.core.constants.FLOAT_ZERO
import com.carmabs.ema.core.navigator.EmaDestination
import com.carmabs.ema.core.state.EmaDataState
import com.carmabs.ema.core.viewmodel.EmaViewModel
import com.google.android.material.appbar.AppBarLayout

/**
 *
 * Base activity to bind and unbind view model
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

abstract class EmaToolbarActivity<S : EmaDataState, VM : EmaViewModel<S, D>, D : EmaDestination> :
    EmaActivity<EmaToolbarActivityBinding, S, VM, D>(){

    final override fun createViewBinding(inflater: LayoutInflater): EmaToolbarActivityBinding {
        return EmaToolbarActivityBinding.inflate(inflater)
    }

    /**
     * Title for toolbar. If it is null the label xml tag in navigation layout is set for the toolbar
     * title, otherwise this title will be set for all fragments inside this activity.
     */
    abstract fun provideFixedToolbarTitle(): String?

    /**
     * Set true if activity use a custom theme to avoid the EmaTheme_NoActionBar theme set up
     */
    protected open val overrideTheme = false

    /**
     * Setup the toolbar
     * @param savedInstanceState for activity recreation
     */
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        //To enable support action bar
        if (!overrideTheme) setTheme(R.style.EmaTheme_NoActionBar)
        super.onCreate(savedInstanceState)
    }

    /**
     * The toobar for the activity
     */
    protected val toolbar: Toolbar by lazy {
        val tool = findViewById<Toolbar>(R.id.emaToolbar)
            ?: throw IllegalArgumentException("You must provide in your activity xml a Toolbar with android:id=@+id/emaToolbar")

        setupToolbar(tool)
        tool
    }

    /**
     * The toolbar container for the activity
     */
    protected val toolbarLayout: AppBarLayout by lazy{
        findViewById<AppBarLayout>(R.id.emaAppBarLayout)
            ?: throw IllegalArgumentException("You must provide in your activity xml an AppBarLayout with android:id=@+ìd/emaAppBarLayout")
    }

    /**
     * Request the action bar setup
     */
    protected fun setupActionBars(){
        //Make this dummy request to launch lazy constructors
        val toolbar = toolbar
        val toolbarLayout = toolbarLayout
    }

    /**
     * Find the toolbar and its container for the activity. The toolbar must have the
     * id=@+id/emaToolbar. The toolbar contaienr [AppBarLayout] must have the id=@+ìd/emaAppBarLayout
     */
    private fun setupToolbar(toolbar: Toolbar) {

        setSupportActionBar(toolbar)
        (navigator as? com.carmabs.ema.android.navigation.EmaNavControllerNavigator)?.navController?.also {
            setupActionBarWithNavController(it)
            it.addOnDestinationChangedListener { _, destination, _ ->
                setToolbarTitle(provideFixedToolbarTitle())
            }
        }

    }

    protected fun setToolbarTitle(title: String?) {
        supportActionBar?.title =
            title ?: provideFixedToolbarTitle() ?: (navigator as? com.carmabs.ema.android.navigation.EmaNavControllerNavigator)?.navController?.currentDestination?.label
    }

    /**
     * Hides the toolbar
     */
    protected fun hideToolbar(gone: Boolean = true, animate: Boolean = true) {
        if (animate) {
            toolbarLayout.animate().apply {
                alpha(FLOAT_ZERO)
                setListener(object : Animator.AnimatorListener {

                    override fun onAnimationEnd(p0: Animator?) {
                        toolbarLayout.visibility = checkVisibility(false, gone)
                    }

                    override fun onAnimationStart(p0: Animator?) {

                    }

                    override fun onAnimationRepeat(p0: Animator?) {

                    }

                    override fun onAnimationCancel(p0: Animator?) {

                    }

                })
            }
        } else {
            toolbarLayout.visibility = if (gone) View.GONE else View.INVISIBLE
        }
    }

    /**
     * Show the toolbar
     */
    protected fun showToolbar(animate: Boolean = true) {
        if(animate) {
            toolbarLayout.animate().apply {
                alpha(FLOAT_ONE)
                setListener(object : Animator.AnimatorListener {

                    override fun onAnimationStart(p0: Animator?) {
                        toolbarLayout.visibility = View.VISIBLE
                    }

                    override fun onAnimationEnd(p0: Animator?) {

                    }

                    override fun onAnimationCancel(p0: Animator?) {

                    }

                    override fun onAnimationRepeat(p0: Animator?) {

                    }
                })
            }
        }
        else{
            toolbarLayout.visibility = View.VISIBLE
        }
    }
}