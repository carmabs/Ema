package com.carmabs.ema.android.ui.dialog

import android.util.Log
import androidx.fragment.app.FragmentManager
import com.carmabs.ema.core.dialog.EmaDialogData
import com.carmabs.ema.core.dialog.EmaDialogListener
import com.carmabs.ema.core.dialog.EmaDialogProvider

/**
 * Simple dialog implementation
 *
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
abstract class EmaAndroidDialogProvider constructor(private val fragmentManager: FragmentManager) :
    EmaDialogProvider {

    private var dialog: EmaBaseDialog<*, EmaDialogData>? = null

    abstract fun generateDialog(dialogData: EmaDialogData?): EmaBaseDialog<*, *>

    override fun show(dialogData: EmaDialogData?) {

        fragmentManager.findFragmentByTag(getTag())?.also {
            dialog = (it as EmaBaseDialog<*,EmaDialogData>).apply {
                updateDialogData(this, dialogData)
            }
        } ?: also {
            if (dialog == null) {
                dialog = generateDialog(dialogData) as EmaBaseDialog<*,EmaDialogData>
            }
            dialog?.let { dialog ->
                updateDialogData(dialog, dialogData)
                //Because fragment manager works asynchronously,
                // check it to avoid that fragment has been added exception
                fragmentManager.executePendingTransactions()
                if (!dialog.isVisible && !dialog.isAdded) {
                    dialog.show(fragmentManager, getTag())
                }

            }
        }
    }

    private fun updateDialogData(
        dialog: EmaBaseDialog<*,EmaDialogData>,
        dialogData: EmaDialogData?
    ) {
        dialog.dialogListener = dialogListener
        dialogData?.also {
            dialog.updateData {
                it
            }
        }
    }

    override fun hide() {
        dialog?.let {
            if (!it.isHidden) {
                Log.d(getTag(), "Alternative dialog totally hidden")
                it.dismiss()
            }
            fragmentManager.beginTransaction().remove(it).commit()
        }


        dialog = null
    }

    override var dialogListener: EmaDialogListener? = null
        set(value) {
            field = value
            dialog?.dialogListener = value
        }

    /**
     * We use dialog class if it is available to handle rotation changes if different dialogs
     * are showing through the same provider
     */
    private fun getTag(): String {

        return dialog?.let { it.javaClass.name.toString() } ?: javaClass.name.toString()
    }


}