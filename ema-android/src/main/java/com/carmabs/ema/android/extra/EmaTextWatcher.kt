package com.carmabs.ema.android.extra

import android.text.Editable
import android.text.TextWatcher
import com.carmabs.ema.core.constants.STRING_EMPTY

/**
 *<p>
 * Copyright (c) 2020, Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 *
 * Date: 2019-11-08
 */

class EmaTextWatcher(private val action: (String) -> Unit) : TextWatcher {

    private var previousText = STRING_EMPTY

    override fun afterTextChanged(s: Editable?) {
        val newText = s?.toString() ?: STRING_EMPTY
        if (newText != previousText)
            action.invoke(newText)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        previousText = s?.toString() ?: STRING_EMPTY
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }
}
