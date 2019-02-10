package com.carmabs.ema.presentation.dialog.base

import androidx.fragment.app.FragmentManager
import com.carmabs.ema.presentation.dialog.DialogData
import com.carmabs.ema.presentation.dialog.DialogListener
import com.carmabs.ema.presentation.dialog.DialogProvider

/**
 * Simple dialog implementation
 *
 * <p>
 * Copyright (C) 2018Babel Sistemas de Información. All rights reserved.
 * </p>
 *
 * @author <a href="mailto:carlos.mateo@babel.es">Carlos Mateo Benito</a>
 */
abstract class BaseDialogProvider constructor(private val fragmentManager: FragmentManager) : DialogProvider {

    private var dialog: BaseDialog? = null
    var listener: DialogListener? = null

    abstract fun generateDialog(): BaseDialog

    override fun show(dialogData: DialogData?) {

        if(dialog==null)
            dialog = generateDialog()

        dialog?.let { dialog ->
            dialog.dialogListener = listener
            dialog.data = dialogData
            if(!dialog.isVisible)
                dialog.show(fragmentManager,javaClass.canonicalName)

        }
    }

    override fun hide() {
        dialog?.let {
            if(it.isVisible) {
                it.dismissAllowingStateLoss()
            }
        }
        dialog = null
    }

    override fun setDialogListener(listener: DialogListener) {
        this.listener = listener
    }

    override fun onDestroyDialog() {
        listener = null
        hide()
    }
}