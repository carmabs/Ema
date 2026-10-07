package com.carmabs.ema.ui.base

import androidx.viewbinding.ViewBinding
import com.carmabs.ema.android.ui.EmaFragment
import com.carmabs.ema.core.model.EmaText
import com.carmabs.ema.core.state.EmaEvent
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.core.viewmodel.EmaViewModel
import com.carmabs.ema.sample.ema.R
import com.carmabs.ema.ui.dialog.AppDialogProvider
import com.carmabs.ema.ui.dialog.error.ErrorDialogData
import com.carmabs.ema.ui.dialog.error.ErrorDialogListener
import com.carmabs.ema.ui.dialog.loading.LoadingDialogData
import com.carmabs.ema.ui.dialog.simple.SimpleDialogData
import com.carmabs.ema.ui.dialog.simple.SimpleDialogListener
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf


/**
 *  *<p>
 * Copyright (c) 2020, Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

abstract class BaseFragment<B : ViewBinding, S : EmaState, VM : EmaViewModel<S, E>, E : EmaEvent> :
    EmaFragment<B, S, VM, E>() {


    private val appDialogProvider: AppDialogProvider by inject {
        parametersOf(childFragmentManager)
    }

    protected fun showLoading(
        data: LoadingDialogData? = null
    ) {
        val loadingData = data ?: LoadingDialogData(
            EmaText.id(R.string.dialog_loading_title),
            EmaText.id(R.string.dialog_loading_message)
        )
        appDialogProvider.dialogListener = null
        appDialogProvider.show(loadingData)
    }

    protected fun showSimpleDialog(
        dialogData: SimpleDialogData,
        dialogListener: SimpleDialogListener
    ) {
        //Listener must be set before showing the dialog, otherwise it is not attached to the new dialog
        appDialogProvider.dialogListener = dialogListener
        appDialogProvider.show(dialogData)
    }

    protected fun showError(
        errorDialogData: ErrorDialogData,
        dialogListener: ErrorDialogListener
    ) {
        //Listener must be set before showing the dialog, otherwise it is not attached to the new dialog
        appDialogProvider.dialogListener = dialogListener
        appDialogProvider.show(errorDialogData)
    }

    /**
     * Shows a message to the user. It is attached to the activity content, so it remains visible
     * although the screen navigates to other one
     */
    protected fun showMessage(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }

    protected fun hideDialog() {
        appDialogProvider.hide()
    }

}
