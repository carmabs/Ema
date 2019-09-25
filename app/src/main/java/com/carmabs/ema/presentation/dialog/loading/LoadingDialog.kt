package com.carmabs.ema.presentation.dialog.loading

import android.view.View
import com.carmabs.ema.R
import com.carmabs.ema.android.ui.dialog.EmaBaseDialog
import kotlinx.android.synthetic.main.dialog_loading.view.*

/**
 * Simple dialog
 *
 * <p>
 * Copyright (C) 2018Babel Sistemas de Información. All rights reserved.
 * </p>
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
class LoadingDialog : EmaBaseDialog<LoadingDialogData>() {

    override fun getLayout(): Int {
        return R.layout.dialog_loading
    }

    override fun setupData(data: LoadingDialogData, view: View) {
        view.tvDialogLoadingTitle.text = data.title
        view.tvDialogLoadingMessage.text = data.message

        isCancelable = true
    }
}