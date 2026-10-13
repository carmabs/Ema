package com.carmabs.emanate.ui.dialog.error

import com.carmabs.emanate.core.dialog.EmaDialogListener

/**
 * Listener based on back OS button or confirm click
 *
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
interface ErrorDialogListener : EmaDialogListener {
    fun onConfirmClicked()
}
