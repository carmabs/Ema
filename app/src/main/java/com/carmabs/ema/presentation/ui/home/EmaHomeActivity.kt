package com.carmabs.ema.presentation.ui.home

import android.widget.Toast
import com.carmabs.ema.R
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.presentation.base.BaseActivity
import org.kodein.di.generic.instance
import java.text.SimpleDateFormat
import java.util.*

/**
 * Project: Ema
 * Created by: cmateob on 20/1/19.
 */
class EmaHomeActivity : BaseActivity<EmaHomeToolbarState,EmaHomeToolbarViewModel,EmaHomeNavigator.Navigation>() {

    override fun getNavGraph(): Int = R.navigation.navigation_ema_home

    override val inputStateKey: String? = null

    override fun onInitialized(viewModel: EmaHomeToolbarViewModel) {

    }

    override fun getToolbarTitle(): String? {
        return "HOLA"
    }

    override val viewModelSeed: EmaHomeToolbarViewModel by instance()

    override val navigator: EmaHomeNavigator by instance()

    override fun onStateNormal(data: EmaHomeToolbarState) {

    }

    override fun onStateLoading(data: EmaExtraData) {

    }



    override fun onSingleEvent(data: EmaExtraData) {
        when(data.type){
             EmaExtraData.DEFAULT_ID -> {
                 (data.extraData as? Pair<*, *>)?.also {
                     (it.second as? Long)?.also { timestamp ->
                         try {
                             val sdf = SimpleDateFormat("hh:mm",Locale.getDefault())
                             val date = sdf.format(Date(timestamp))
                             Toast.makeText(this,String.format(
                                     getString(R.string.ema_home_last_user),it.first,date
                             ),Toast.LENGTH_SHORT).show()
                             return
                         } catch (e: Exception) { }
                     }

                 }
             }
        }
    }

    override fun onStateError(error: Throwable) {

    }
}