package com.carmabs.ema.presentation.dialog

/**
 * Listener based on back OS button or confirm click
 *
 * <p>
 * Copyright (C) 2018Babel Sistemas de Información. All rights reserved.
 * </p>
 *
 * @author <a href="mailto:carlos.mateo@babel.es">Carlos Mateo Benito</a>
 */
interface DialogListener {
    fun onBackPressed()
    fun onConfirmClicked()

}