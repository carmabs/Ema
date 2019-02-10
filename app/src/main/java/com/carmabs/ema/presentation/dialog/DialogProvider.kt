package com.carmabs.ema.presentation.dialog



/**
 * Abstract class to show a dialog
 *
 * <p>
 * Copyright (C) 2018Babel Sistemas de Información. All rights reserved.
 * </p>
 *
 * @author <a href="mailto:carlos.mateo@babel.es">Carlos Mateo Benito</a>
 */
interface DialogProvider {
    fun show(dialogData: DialogData?=null)
    fun hide()
    fun setDialogListener(listener: DialogListener)
    fun onDestroyDialog()


}