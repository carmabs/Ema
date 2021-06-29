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
abstract class EmaAndroidDialogProvider constructor(private val fragmentManager: FragmentManager) : EmaDialogProvider {

    private var dialog: EmaBaseDialog<EmaDialogData>? = null

    abstract fun generateDialog(dialogData: EmaDialogData?): EmaBaseDialog<*>

    override fun show(dialogData: EmaDialogData?) {

        if (dialog == null)
            dialog = generateDialog(dialogData) as EmaBaseDialog<EmaDialogData>

        dialog?.let { dialog ->
            dialog.dialogListener = dialogListener
            dialogData?.also {
                dialog.updateData {
                    it
                }
            }
            if (!dialog.isVisible)
                dialog.show(fragmentManager, getTag())

        }
    }

    override fun hide() {
        dialog?.let {
            if (!it.isHidden) {
                Log.d(this.javaClass.name, "Alternative dialog totally hidden")
                it.dismissAllowingStateLoss()
            }
        } ?: also { _ ->
            val oldDialog = fragmentManager.findFragmentByTag(getTag())
            oldDialog?.also {
                fragmentManager.beginTransaction().remove(it).commit()
            }

        }
        dialog = null
    }

    override var dialogListener: EmaDialogListener? = null
        set(value) {
            field = value
            dialog?.dialogListener = value
        }

    private fun getTag():String{
        return javaClass.name.toString()
    }


}