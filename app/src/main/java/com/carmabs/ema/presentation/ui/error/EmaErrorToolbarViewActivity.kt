package com.carmabs.ema.presentation.ui.error

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.carmabs.ema.R
import com.carmabs.ema.android.extension.dpToPx
import com.carmabs.ema.android.extension.getColor
import com.carmabs.ema.android.extension.getFormattedString
import com.carmabs.ema.android.ui.EmaActivity
import com.carmabs.ema.android.viewmodel.EmaAndroidViewModel
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.presentation.injection.activityInjection
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import kotlin.math.roundToInt


/**
 *
 * Activity that inherits from EmaActivity, overrideTheme is false, so we take EmaTheme, for that reason
 * status bar in this screen has EmaTheme colorPrimaryDark color
 * We can override toolbar background
 *
 *
 **/
class EmaErrorToolbarViewActivity : EmaActivity<EmaErrorToolbarState, EmaErrorToolbarViewModel, EmaErrorNavigator.Navigation>() {

    override val navGraph: Int = R.navigation.navigation_ema_error

    override fun provideFixedToolbarTitle(): String? = getString(R.string.error_toolbar_title)

    override val androidViewModelSeed: EmaAndroidViewModel<EmaErrorToolbarViewModel> by instance<EmaAndroidErrorToolbarViewModel>()

    override val navigator: EmaErrorNavigator by instance()

    override fun injectActivityModule(kodein: Kodein.MainBuilder): Kodein.Module? = activityInjection(this)

    override fun onCreateActivity(savedInstanceState: Bundle?) {
        super.onCreateActivity(savedInstanceState)
        configureToolbar()

    }

    private fun configureToolbar() {

        //With EMA activity you can customize the toolbar
        toolbar.apply {
            val whiteColor = android.R.color.white.getColor(context)
            setBackgroundColor(R.color.colorPrimary.getColor(context))
            logo = getDrawable(R.drawable.ic_error_toolbar)
            setTitleTextColor(whiteColor)
            titleMarginStart = resources.getDimension(R.dimen.space_medium).roundToInt().dpToPx(context)

        }
    }

    override fun onStateNormal(data: EmaErrorToolbarState) {
        checkToolbarVisibility(data)
    }

    private fun checkToolbarVisibility(data: EmaErrorToolbarState) {
        if (data.visibility)
            showToolbar()
        else
            hideToolbar()
    }

    override fun onStateAlternative(data: EmaExtraData) {

    }

    override fun onSingleEvent(data: EmaExtraData) {
        Toast.makeText(this, R.string.error_user_created.getFormattedString(this, data.extraData as Int), Toast.LENGTH_SHORT).show()

    }

    override fun onStateError(error: Throwable) {

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
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