package com.carmabs.ema.presentation.dialog.simple

import com.carmabs.ema.presentation.dialog.DialogListener

/**
 * Listener based on back OS button or confirm click
 *
 * <p>
 * Copyright (C) 2018Babel Sistemas de Información. All rights reserved.
 * </p>
 *
 * @author <a href="mailto:carlos.mateo@babel.es">Carlos Mateo Benito</a>
 */
interface SimpleDialogListener : DialogListener {
    fun onCancelClicked()

}