package com.carmabs.ema.presentation.dialog.simple

import androidx.fragment.app.FragmentManager
import com.carmabs.ema.android.ui.dialog.EmaBaseDialog
import com.carmabs.ema.android.ui.dialog.EmaBaseDialogProvider

/**
 * Simple dialog implementation
 *
 * <p>
 * Copyright (C) 2018Babel Sistemas de Información. All rights reserved.
 * </p>
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */

class SimpleDialogProvider constructor(fragmentManager: FragmentManager) : EmaBaseDialogProvider(fragmentManager) {
    override fun generateDialog(): EmaBaseDialog<*> = SimpleDialog()
}