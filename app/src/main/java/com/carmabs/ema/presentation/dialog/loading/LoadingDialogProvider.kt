package com.carmabs.ema.presentation.dialog.loading

import androidx.fragment.app.FragmentManager
import com.carmabs.ema.android.ui.dialog.EmaAndroidDialogProvider
import com.carmabs.ema.android.ui.dialog.EmaBaseDialog
import com.carmabs.ema.core.dialog.EmaDialogData

/**
 * Simple dialog implementation
 *
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */

class LoadingDialogProvider constructor(fragmentManager: FragmentManager) : EmaAndroidDialogProvider(fragmentManager)
{
    override fun generateDialog(dialogData: EmaDialogData?): EmaBaseDialog<*> =  LoadingDialog()
}