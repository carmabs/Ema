package com.carmabs.ema.presentation.dialog.simple

import com.carmabs.ema.core.dialog.EmaDialogListener

/**
 * Listener based on back OS button or confirm click
 *
 * <p>
 * Copyright (C) 2018Babel Sistemas de Información. All rights reserved.
 * </p>
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
interface SimpleDialogListener : EmaDialogListener {
    fun onCancelClicked()
    fun onConfirmClicked()

}