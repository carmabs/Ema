package com.carmabs.ema.presentation.dialog.loading
import android.view.View
import com.carmabs.ema.presentation.dialog.base.BaseDialog
import kotlinx.android.synthetic.main.dialog_loading.view.*
import com.carmabs.ema.R

/**
 * Simple dialog
 *
 * <p>
 * Copyright (C) 2018Babel Sistemas de Información. All rights reserved.
 * </p>
 *
 * @author <a href="mailto:carlos.mateo@babel.es">Carlos Mateo Benito</a>
 */
class LoadingDialog : BaseDialog() {

    override fun getLayout(): Int {
        return R.layout.dialog_loading
    }

    override fun setupUI(view: View) {
        (data as? LoadingDialogData)?.let {
            view.tvDialogLoadingTitle.text = it.title
            view.tvDialogLoadingMessage.text = it.message
        }

        isCancelable = true
    }
}