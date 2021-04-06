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

    override fun View.setup(data: LoadingDialogData) {
        tvDialogLoadingTitle.text = data.title
        tvDialogLoadingMessage.text = data.message

        isCancelable = false
    }

    override fun createInitialState(): LoadingDialogData {
        return LoadingDialogData()
    }
}