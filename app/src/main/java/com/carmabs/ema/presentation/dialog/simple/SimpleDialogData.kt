package com.carmabs.ema.presentation.dialog.simple

import android.graphics.drawable.Drawable
import com.carmabs.ema.core.dialog.EmaDialogData

/**
 * Data for simple dialog
 *
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

data class SimpleDialogData(
        val title: String = "",
        val message: String = "",
        val accept: String = "",
        val cancel: String = "",
        val showCross: Boolean = true,
        val image: Drawable? = null,
        override val proportionWidth: Float? = null,
        override val proportionHeight: Float? = null) : EmaDialogData