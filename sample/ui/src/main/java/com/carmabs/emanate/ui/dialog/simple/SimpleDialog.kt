package com.carmabs.emanate.ui.dialog.simple

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.carmabs.emanate.android.extension.string
import com.carmabs.emanate.android.ui.dialog.EmaDialog
import com.carmabs.emanate.core.model.EmaText
import com.carmabs.emanate.sample.ui.R
import com.carmabs.emanate.sample.ui.databinding.DialogSimpleBinding

/**
 * Simple dialog
 *
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
class SimpleDialog : EmaDialog<DialogSimpleBinding, SimpleDialogData>() {

    override fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?): DialogSimpleBinding =
        DialogSimpleBinding.inflate(inflater, container, false)

    override fun DialogSimpleBinding.setup(data: SimpleDialogData) {
        with(data) {
            (dialogListener as? SimpleDialogListener)?.let { listener ->
                bDialogSimpleNo.setOnClickListener { listener.onCancelClicked() }
                ivDialogSimpleCross.setOnClickListener { listener.onCancelClicked() }
                bDialogSimpleYes.setOnClickListener { listener.onConfirmClicked() }
            }

            tvDialogSimpleTitle.text = title.string(requireContext())

            ivDialogSimpleCross.visibility = if (showCross) View.VISIBLE else View.GONE

            tvDialogSimpleMessage.text = message.string(requireContext())

            bDialogSimpleYes.text = resources.getString(R.string.dialog_accept)

            ivDialogSimple.visibility =
                image?.let {
                    ivDialogSimple.setImageResource(it)
                    View.VISIBLE
                } ?: View.GONE

            if (!showCancel) {
                bDialogSimpleNo.visibility = View.GONE
            }

            bDialogSimpleNo.text = resources.getString(R.string.dialog_cancel)

            isCancelable = !isModal
        }
    }

    override fun createInitialState(): SimpleDialogData = SimpleDialogData(
        EmaText.empty(),
        EmaText.empty()
    )
}
