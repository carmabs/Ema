package com.carmabs.emanate.ui.dialog.loading

import android.view.LayoutInflater
import android.view.ViewGroup
import com.carmabs.emanate.android.extension.string
import com.carmabs.emanate.android.ui.dialog.EmaDialog
import com.carmabs.emanate.sample.ui.databinding.DialogLoadingBinding

/**
 * Simple dialog
 *
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
class LoadingDialog : EmaDialog<DialogLoadingBinding, LoadingDialogData>() {

    override fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?): DialogLoadingBinding =
        DialogLoadingBinding.inflate(inflater, container, false)

    override fun DialogLoadingBinding.setup(data: LoadingDialogData) {
        tvDialogLoadingTitle.text = data.title.string(requireContext())
        tvDialogLoadingMessage.text = data.message.string(requireContext())

        isCancelable = !data.isModal
    }

    override fun createInitialState(): LoadingDialogData = LoadingDialogData()
}
