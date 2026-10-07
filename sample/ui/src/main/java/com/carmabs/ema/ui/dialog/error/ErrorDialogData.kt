package com.carmabs.ema.ui.dialog.error

import com.carmabs.ema.core.dialog.EmaDialogData
import com.carmabs.ema.core.model.EmaText

/**
 *  *<p>
 * Copyright (c) 2020, Carmabs. All rights reserved.
 * </p>
 *
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

data class ErrorDialogData(
    val title: EmaText =  EmaText.empty(),
    val message: EmaText = EmaText.empty(),
    override val proportionWidth: Float? = 0.85f,
    override val proportionHeight: Float? = null,
    override val isModal: Boolean = true
) : EmaDialogData