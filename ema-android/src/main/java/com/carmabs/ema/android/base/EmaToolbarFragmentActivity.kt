package com.carmabs.ema.android.base

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.appbar.AppBarLayout
import com.carmabs.ema.android.R

/**
 * [EmaFragmentActivity]  with toolbar support
 */
abstract class EmaToolbarFragmentActivity : EmaFragmentActivity() {

    /**
     * The toobar for the activity
     */
    protected lateinit var toolbar: Toolbar
        private set

    /**
     * The toolbar container for the activity
     */
    private lateinit var toolbarLayout: AppBarLayout

    /**
     * Setup the toolbar
     * @param savedInstanceState for activity recreation
     */
    override fun createActivity(savedInstanceState: Bundle?) {
        setupNavigation()
        setupToolbar()
    }

    /**
     * Setup the navigation path for navigation architecture components
     */
    private fun setupNavigation() {
        navController.setGraph(getNavGraph())
    }

    /**
     * Find the toolbar and its container for the activity. The toolbar must have the
     * id=@+id/emaToolbar. The toolbar contaienr [AppBarLayout] must have the id=@+ìd/emaAppBarLayout
     */
    private fun setupToolbar() {
        val tbToolbar = findViewById<Toolbar>(R.id.emaToolbar)
                ?: throw IllegalArgumentException("You must provide in your activity xml a Toolbar with android:id=@+id/emaToolbar")
        val lToolbar = findViewById<AppBarLayout>(R.id.emaAppBarLayout)
                ?: throw IllegalArgumentException("You must provide in your activity xml an AppBarLayout with android:id=@+ìd/emaAppBarLayout")

        setSupportActionBar(tbToolbar)
        toolbarLayout = lToolbar
        toolbar = tbToolbar
        setupActionBarWithNavController(navController)
    }

    /**
     * Hides the toolbar
     */
    protected fun hideToolbar() {
        toolbarLayout.visibility = View.GONE
    }

    /**
     * Show the toolbar
     */
    protected fun showToolbar() {
        toolbarLayout.visibility = View.VISIBLE
    }

    /**
     * Get the navigation resource for the activity [R.navigation]
     */
    abstract fun getNavGraph(): Int

    /**
     * Get the toolbar title
     */
    abstract fun getToolbarTitle(): String?

    /**
     * The layout set up for the activity
     */
    override fun getLayout(): Int {
        return R.layout.ema_toolbar_activity
    }
}