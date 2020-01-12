package com.carmabs.ema.android.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.navigation.ui.setupActionBarWithNavController
import com.carmabs.ema.android.R
import com.google.android.material.appbar.AppBarLayout

/**
 * [EmaFragmentActivity]  with toolbar support
 */
abstract class EmaToolbarFragmentActivity : EmaFragmentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {

        //To enable support action bar
        if (!overrideTheme) setTheme(R.style.EmaTheme_NoActionBar)
        super.onCreate(savedInstanceState)
    }

    /**
     * The toobar for the activity
     */
    protected lateinit var toolbar: Toolbar
        private set

    /**
     * The toolbar container for the activity
     */
    protected lateinit var toolbarLayout: AppBarLayout


    /**
     * Title for toolbar. If it is null the label inside navigation layout for the view is set.
     */
    abstract fun provideToolbarTitle(): String?

    /**
     * Setup the toolbar
     * @param savedInstanceState for activity recreation
     */
    override fun onCreateActivity(savedInstanceState: Bundle?) {
        super.onCreateActivity(savedInstanceState)
        setupToolbar()
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
        navController.addOnDestinationChangedListener { _, destination, _ ->
           setToolbarTitle(provideToolbarTitle())

        }
    }

    protected fun setToolbarTitle(title:String?){
        title?.also { supportActionBar?.title = it }
    }

    /**
     * Hides the toolbar
     */
    protected open fun hideToolbar(gone: Boolean = true) {
        toolbarLayout.visibility = if (gone) View.GONE else View.INVISIBLE
    }

    /**
     * Show the toolbar
     */
    protected open fun showToolbar() {
        toolbarLayout.visibility = View.VISIBLE
    }

    /**
     * The layout set up for the activity
     */
    override fun getLayout(): Int {
        return R.layout.ema_toolbar_activity
    }

    /**
     * Set true if activity use a custom theme to avoid the EmaTheme_NoActionBar theme set up
     */
    protected open val overrideTheme = false
}