package com.carmabs.ema.presentation.dialog.loading

import androidx.fragment.app.FragmentManager
import com.carmabs.ema.android.ui.dialog.EmaBaseDialog
import com.carmabs.ema.android.ui.dialog.EmaBaseDialogProvider

/**
 * Simple dialog implementation
 *
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */

class LoadingDialogProvider constructor(fragmentManager: FragmentManager) : EmaBaseDialogProvider(fragmentManager)
{
    override fun generateDialog(): EmaBaseDialog<*> = LoadingDialog()
}