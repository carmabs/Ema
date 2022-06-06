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
import androidx.viewbinding.ViewBinding
import com.carmabs.ema.android.delegates.emaStateDelegate
import com.carmabs.ema.android.extension.getScreenMetrics
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
abstract class EmaBaseDialog<B : ViewBinding, T : EmaDialogData> : DialogFragment(), DIAware,
    DialogInterface.OnShowListener {

    private var _binding: B? = null
    protected val binding
        get() = _binding!!

    companion object {
        private const val KEY_DIALOG_DATA = "KEY_DIALOG_DATA"
    }

    override val di: DI by closestDI {
        requireActivity()
    }

    var dialogListener: EmaDialogListener? = null

    var data: T by emaStateDelegate {
        createInitialState()
    }
    private set

    private var isDismissed: Boolean = false

    protected open val disableBackButton
        get() = !isCancelable


    /**
     * Specify the ViewBinding to be inflated in the [EmaBaseDialog.onCreateView].
     */
    abstract fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?): B

    /**
     * Setup data for UI
     */
    protected abstract fun B.setup(data: T)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =  super.onCreateDialog(savedInstanceState)
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
        _binding?.setup(data)
        return data
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setup(data)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = createViewBinding(inflater, container)
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestFeature(Window.FEATURE_NO_TITLE)
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.also { win ->
            val display = getScreenMetrics(requireContext())
            val size = Point().apply {
                x = display.widthPixels
                y = display.heightPixels
            }
            data.run {
                val width = proportionWidth?.let { (it * size.x).toInt() }
                    ?: ViewGroup.LayoutParams.WRAP_CONTENT
                val height = proportionHeight?.let { (it * size.y).toInt() }
                    ?: ViewGroup.LayoutParams.WRAP_CONTENT
                win.setLayout(width, height)
                isCancelable = !isModal
                dialog?.setCanceledOnTouchOutside(isCancelable)
                dialog?.setOnCancelListener {
                    dialogListener?.onOutsidePressed()
                }
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
        super.onDestroyView()
        _binding = null
        dialogListener = null
    }
}