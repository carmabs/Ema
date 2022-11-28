package com.carmabs.ema.android.permission

import android.app.AlertDialog

/**
 * Created by Carlos Mateo Benito on 2022-08-12.
 *
 * <p>
 * Copyright (c) 2022 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
sealed interface InfoDialogType {
    data class Default(val title: String, val message: String) : InfoDialogType
    data class CustomDialog(
        val alertDialog: AlertDialog,
        val onAcceptClickListener: (() -> Unit)? = null
    ) :
        InfoDialogType
}