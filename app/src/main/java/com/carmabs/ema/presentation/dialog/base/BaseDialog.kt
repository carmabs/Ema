package com.carmabs.ema.presentation.dialog.base

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Point
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.carmabs.ema.presentation.dialog.DialogData
import com.carmabs.ema.presentation.dialog.DialogListener

/**
 *
 * <p>
 * Copyright (C) 2018 Babel Sistemas de Información. All rights reserved.
 * </p>
 *
 * @author <a href="mailto:carlos.mateo@babel.es">Carlos Mateo Benito</a>
 */
abstract class BaseDialog: DialogFragment(), DialogInterface.OnShowListener {

    val TAG = "BASE_DIALOG"

    var dialogListener: DialogListener? = null
    var data: DialogData?=null

    private var isDismissed: Boolean = false


    /**
     * Specify the layout to be inflated in the [BaseDialog.onCreateView].
     */
    protected abstract fun getLayout(): Int

    /**
     * Setup UI of the view.
     * @param view
     */
    protected abstract fun setupUI(view: View)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener(this)
        dialog.setOnKeyListener { _, keyCode, event ->
            dialogListener?.let {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
                    dismissAllowingStateLoss()
                    it.onBackPressed()
                }
            }
            false
        }

        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(getLayout(), container, false)
        setupUI(view)

        return view
    }

    override fun onResume() {
        super.onResume()
        val display = dialog?.window?.windowManager?.defaultDisplay
        val size = Point()
        display?.getSize(size)
        val width = (3 / 4f) * size.x
        dialog?.window?.setLayout(width.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)

    }

    override fun onShow(p0: DialogInterface?) {
        if (isDismissed)
            dismissAllowingStateLoss()
    }

    override fun show(manager: FragmentManager, tag: String?) {
        val oldFragment = manager.findFragmentByTag(tag)
        val ft = manager.beginTransaction()
        oldFragment?.let { ft.remove(oldFragment) }
        ft.add(this, tag)
        ft.commit()
    }

    override fun onSaveInstanceState(outState: Bundle) {

    }
}