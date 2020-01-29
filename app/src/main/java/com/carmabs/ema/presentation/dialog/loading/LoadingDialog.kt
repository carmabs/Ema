package com.carmabs.ema.presentation.dialog.loading

import android.view.View
import com.carmabs.ema.R
import com.carmabs.ema.android.ui.dialog.EmaBaseDialog
import kotlinx.android.synthetic.main.dialog_loading.view.*

/**
 * Simple dialog
 *
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
class LoadingDialog : EmaBaseDialog<LoadingDialogData>() {

    override val layoutId: Int = R.layout.dialog_loading

    override fun setupData(data: LoadingDialogData, view: View) {
        view.tvDialogLoadingTitle.text = data.title
        view.tvDialogLoadingMessage.text = data.message

        isCancelable = false
    }
}