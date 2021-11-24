package com.carmabs.ema.android.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.carmabs.ema.android.delegates.emaStateDelegate
import com.carmabs.ema.core.dialog.EmaDialogData
import com.carmabs.ema.core.dialog.EmaDialogListener
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI


/**
 * Base dialog class to implement dialogs
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
abstract class EmaBaseDialog<T : EmaDialogData> : DialogFragment(), DIAware,
    DialogInterface.OnShowListener {

    companion object {
        private const val KEY_DIALOG_DATA = "KEY_DIALOG_DATA"
    }

    override val di: DI by closestDI {
        requireActivity()
    }


    var dialogListener: EmaDialogListener? = null

    private var data: T by emaStateDelegate {
        createInitialState()
    }

    private var isDismissed: Boolean = false

    private var contentView: View? = null

    protected open val disableBackButton
        get() = !isCancelable


    /**
     * Specify the layout to be inflated in the [EmaBaseDialog.onCreateView].
     */
    protected abstract val layoutId: Int

    /**
     * Setup data for UI
     */
    protected abstract fun View.setup(data: T)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        (savedInstanceState?.getSerializable(KEY_DIALOG_DATA) as? T)?.also {
            data = it
        }
        dialog.setOnShowListener(this)
        dialog.setOnKeyListener { _, keyCode, event ->
            dialogListener?.let {
                if (!disableBackButton && keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
                    dismissAllowingStateLoss()
                    it.onBackPressed()
                }
            }
            false
        }

        return dialog
    }

    fun updateData(updateAction: T.() -> T): T {
        data = data.let(updateAction)
        contentView?.setup(data)
        return data
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentView = view
        view.setup(data)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(layoutId, container, false)
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestFeature(Window.FEATURE_NO_TITLE)
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.also { win ->
            val display = win.windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)

            data.run {
                val width = proportionWidth?.let { (it * size.x).toInt() }
                    ?: ViewGroup.LayoutParams.WRAP_CONTENT
                val height = proportionHeight?.let { (it * size.y).toInt() }
                    ?: ViewGroup.LayoutParams.WRAP_CONTENT
                win.setLayout(width, height)
            }
        }


    }

    override fun onShow(p0: DialogInterface?) {
        if (isDismissed)
            dismissAllowingStateLoss()
    }

    override fun show(manager: FragmentManager, tag: String?) {
        manager.apply {
            val oldFragment = findFragmentByTag(tag)
            val ft = beginTransaction()
            oldFragment?.let { ft.remove(oldFragment) }
            ft.add(this@EmaBaseDialog, tag)
            ft.commit()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(KEY_DIALOG_DATA, data)
    }

    protected abstract fun createInitialState(): T

    override fun onDestroyView() {
        contentView = null
        dialogListener = null
        super.onDestroyView()
    }
}