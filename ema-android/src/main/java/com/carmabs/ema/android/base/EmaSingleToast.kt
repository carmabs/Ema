package com.carmabs.ema.android.base

import android.content.Context
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.widget.Toast
import com.carmabs.ema.core.constants.INT_ONE
import com.carmabs.ema.core.constants.INT_ZERO


/**
 * Created by Carlos Mateo Benito on 5/11/21.
 *
 * <p>
 * Copyright (c) 2021 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>* Navigator to handle navigation through navController
 *
 * Create unique Toast to prevent overlapping when various Toast are launched
 * at same time
 */
class EmaSingleToast {
    companion object {
        private var singleToast: Toast? = null
        fun show(
            context: Context,
            message: String,
            duration: Int,
            textAlignment: Layout.Alignment = Layout.Alignment.ALIGN_CENTER
        ) {
            singleToast?.cancel()
            val alignedMessage: Spannable = SpannableString(message)
            alignedMessage.setSpan(
                AlignmentSpan.Standard(textAlignment),
                INT_ZERO, message.length - INT_ONE,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            singleToast = Toast.makeText(context, alignedMessage, duration).apply {
                show()
            }
        }
    }
}

fun Context.showToast(message: String, duration: Int) {
    EmaSingleToast.show(this, message, duration)
}