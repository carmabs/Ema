package com.carmabs.ema.presentation.base.compose

import androidx.compose.runtime.Composable
import com.carmabs.ema.compose.ui.EmaComposableScreenContent
import com.carmabs.ema.core.action.EmaAction
import com.carmabs.ema.core.model.EmaText
import com.carmabs.ema.core.state.EmaEvent
import com.carmabs.ema.core.state.EmaState
import com.carmabs.ema.presentation.dialog.error.ErrorDialogData
import com.carmabs.ema.presentation.dialog.error.ErrorDialogListener
import com.carmabs.ema.presentation.dialog.loading.LoadingDialogData
import com.carmabs.ema.presentation.dialog.simple.SimpleDialogData
import com.carmabs.ema.presentation.dialog.simple.SimpleDialogListener
import com.carmabs.ema.presentation.ui.compose.ErrorDialogComposable
import com.carmabs.ema.presentation.ui.compose.LoadingDialogComposable
import com.carmabs.ema.presentation.ui.compose.SimpleDialogComposable
import com.carmabs.ema.sample.ema.R

abstract class BaseScreenComposable<S : EmaState, A : EmaAction.Screen, E : EmaEvent> :
    EmaComposableScreenContent<S, A, E> {

    @Composable
    protected fun ShowDialog(data: SimpleDialogData,listener: SimpleDialogListener) {
        SimpleDialogComposable(dialogData = data, listener)
    }

    @Composable
    protected fun ShowError(data: ErrorDialogData, listener: ErrorDialogListener) {
        ErrorDialogComposable(dialogData = data, dialogListener = listener)
    }

    @Composable
    protected fun ShowLoading(loadingDialogData: LoadingDialogData?=null) {
        LoadingDialogComposable(
            dialogData = loadingDialogData ?: LoadingDialogData(
                title = EmaText.id(id = R.string.dialog_loading_title),
                message = EmaText.id(id = R.string.dialog_loading_message)
            )
        )
    }
}
