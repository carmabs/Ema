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
abstract class EmaBaseDialogProvider constructor(private val fragmentManager: FragmentManager) : EmaDialogProvider {

    private var dialog: EmaBaseDialog<EmaDialogData>? = null

    abstract fun generateDialog(): EmaBaseDialog<*>

    override fun show(dialogData: EmaDialogData?) {

        if(dialog==null)
            dialog = generateDialog() as EmaBaseDialog<EmaDialogData>

        dialog?.let { dialog ->
            dialog.dialogListener = dialogListener
            dialog.data = dialogData
            if(!dialog.isVisible)
                dialog.show(fragmentManager,javaClass.canonicalName.hashCode().toString())

        }
    }

    override fun hide() {
        dialog?.let {
            if(!it.isHidden) {
                Log.d(this.javaClass.name,"Loading dialog totally hidden")
                it.dismissAllowingStateLoss()
            }
        }
        dialog = null
    }

    override var dialogListener: EmaDialogListener? = null
            set(value) {
                field = value
                dialog?.dialogListener = value
            }
}