package com.carmabs.ema.presentation.dialog.simple

import android.graphics.drawable.Drawable
import com.carmabs.ema.core.dialog.EmaDialogData


/**
 *  *<p>
 * Copyright (c) 2020, Carmabs. All rights reserved.
 * </p>
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
        override val proportionWidth: Float? = 7/10f,
        override val proportionHeight: Float? = null) : EmaDialogData