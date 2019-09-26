package com.carmabs.ema.presentation.dialog.loading

import com.carmabs.ema.core.dialog.EmaDialogData

/**
 * TODO: Add a class header comment.
 *
*
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

data class LoadingDialogData(
        val title: String = "",
        val message: String = "",
        override val proportionWidth: Float? = 7/10f,
        override val proportionHeight: Float? = null) : EmaDialogData