package com.carmabs.emanate.ui.dialog.simple

import androidx.fragment.app.FragmentManager
import com.carmabs.emanate.android.ui.dialog.EmaAndroidDialogProvider
import com.carmabs.emanate.android.ui.dialog.EmaDialog
import com.carmabs.emanate.core.dialog.EmaDialogData

/**
 * Simple dialog implementation
 *
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */

class SimpleDialogProvider constructor(fragmentManager: FragmentManager) : EmaAndroidDialogProvider(fragmentManager) {
    override fun generateDialog(dialogData: EmaDialogData?): EmaDialog<*, *> = SimpleDialog()
}
