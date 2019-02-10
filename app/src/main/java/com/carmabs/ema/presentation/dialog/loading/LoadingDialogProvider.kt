package com.carmabs.ema.presentation.dialog.loading

import androidx.fragment.app.FragmentManager
import com.carmabs.ema.presentation.dialog.base.BaseDialog
import com.carmabs.ema.presentation.dialog.base.BaseDialogProvider

/**
 * Simple dialog implementation
 *
 * <p>
 * Copyright (C) 2018Babel Sistemas de Información. All rights reserved.
 * </p>
 *
 * @author <a href="mailto:carlos.mateo@babel.es">Carlos Mateo Benito</a>
 */

class LoadingDialogProvider constructor(fragmentManager: FragmentManager) : BaseDialogProvider(fragmentManager)
{
    override fun generateDialog(): BaseDialog = LoadingDialog()
}