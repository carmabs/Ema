package com.carmabs.ema.presentation.ui.error

import android.animation.LayoutTransition
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.carmabs.ema.android.extension.toDp
import com.carmabs.ema.android.ui.EmaView
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.presentation.base.BaseActivity
import com.carmabs.ema.presentation.ui.home.EmaHomeNavigator
import kotlin.math.roundToInt
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import com.carmabs.ema.R
import com.carmabs.ema.core.concurrency.DefaultConcurrencyManager
import org.kodein.di.generic.instance


/**
 *
 * We show how to attach  a view model to any view through EmaView interface
 *
 **/
class EmaErrorViewActivity : BaseActivity(), EmaView<EmaToolbarState, EmaToolbarViewModel, EmaHomeNavigator.Navigation> {

    override fun getNavGraph(): Int = R.navigation.navigation_ema_error

    override fun getToolbarTitle(): String? = getString(R.string.error_bad_credentials)

    override val viewModelSeed: EmaToolbarViewModel by instance()

    override val navigator: EmaHomeNavigator by instance()

    override val inputState: EmaToolbarState? = null

    private lateinit var vm: EmaToolbarViewModel

    override fun createActivity(savedInstanceState: Bundle?) {
        (getContentLayout() as ViewGroup).layoutTransition = LayoutTransition()
        super.createActivity(savedInstanceState)
        //Very important to call this method to initialize the view model in right context
        initializeViewModel(this)
        configureToolbar()
    }

    private fun configureToolbar() {
        toolbar.apply {
            val whiteColor = ContextCompat.getColor(applicationContext, android.R.color.white)
            setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
            logo = getDrawable(R.drawable.ic_error_toolbar)
            setTitleTextColor(whiteColor)
            titleMarginStart = resources.getDimension(R.dimen.space_medium).roundToInt().toDp(context)
        }

        val concurrencyManager = DefaultConcurrencyManager()
        concurrencyManager.cancelTask(concurrencyManager.launch(fullException = false) {

        })
    }

    override fun onViewModelInitalized(viewModel: EmaToolbarViewModel) {
        vm = viewModel
    }

    override fun onStateNormal(data: EmaToolbarState) {
        checkToolbarVisibility(data)
    }

    private fun checkToolbarVisibility(data: EmaToolbarState) {
        if(data.visibility)
            showToolbar()
        else
            hideToolbar()
    }

    override fun onStateLoading(data: EmaExtraData) {

    }

    override fun onSingleEvent(data: EmaExtraData) {

    }

    override fun onStateError(error: Throwable) {

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_error, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {

            R.id.menu_action_error_hide_toolbar -> {
                vm.onActionMenuHideToolbar()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }


    }
}