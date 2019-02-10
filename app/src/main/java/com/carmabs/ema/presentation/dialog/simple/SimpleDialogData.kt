package com.carmabs.ema.presentation.dialog.simple

import android.graphics.drawable.Drawable
import com.carmabs.ema.presentation.dialog.DialogData


/**
 * TODO: Add a class header comment.
 *
 * <p>
 * Copyright (c) 2019, Babel Sistemas de Información. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:carlos.mateo@babel.es”>Carlos Mateo</a>
 */

data class SimpleDialogData(
        val title: String = "",
        val message: String = "",
        val accept:String= "",
        val cancel:String = "",
        val showCross:Boolean = true,
        val image: Drawable? = null) : DialogData