package com.carmabs.ema.presentation.dialog.loading

import com.carmabs.ema.core.dialog.EmaDialogData


/**
 * Data for loading dialog
 *

 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

data class LoadingDialogData(
        val title: String = "",
        val message: String = "",
        override val proportionWidth: Float? = null,
        override val proportionHeight: Float? = null) : EmaDialogData