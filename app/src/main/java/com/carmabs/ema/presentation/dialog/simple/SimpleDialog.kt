package com.carmabs.ema.presentation.dialog.simple

import android.view.View
import com.carmabs.ema.R
import com.carmabs.ema.android.ui.dialog.EmaBaseDialog
import kotlinx.android.synthetic.main.dialog_simple.view.*


/**
 * Simple dialog
 *
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
class SimpleDialog : EmaBaseDialog<SimpleDialogData>() {

    override val layoutId: Int = R.layout.dialog_simple

    override fun View.setup(data: SimpleDialogData) {
        with(data) {
            (dialogListener as? SimpleDialogListener)?.let { listener ->
                bDialogSimpleNo.setOnClickListener { listener.onCancelClicked() }
                ivDialogSimpleCross.setOnClickListener { listener.onCancelClicked() }
                bDialogSimpleYes.setOnClickListener { listener.onConfirmClicked() }
            }

            tvDialogSimpleTitle!!.text = title

            if (showCross)
                ivDialogSimpleCross.visibility = if (showCross) View.VISIBLE else View.GONE

            tvDialogSimpleMessage!!.text = message

            bDialogSimpleYes.text = accept

            ivDialogSimple.visibility =
                    image?.let {
                        ivDialogSimple.setImageDrawable(it)
                        View.VISIBLE
                    } ?: View.GONE

            if (cancel.isEmpty()) {
                bDialogSimpleNo.visibility = View.GONE
            }

            bDialogSimpleNo.text = cancel

            isCancelable = false
        }
    }

    override fun createInitialState(): SimpleDialogData = SimpleDialogData()
}