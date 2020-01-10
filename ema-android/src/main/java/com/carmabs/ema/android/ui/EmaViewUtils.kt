package com.carmabs.ema.android.ui

import android.content.Context
import android.view.View
import androidx.annotation.StringRes

/**
 * <p>
 * Copyright (c) 2019, Babel Sistemas de Información. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:carlos.mateo@babel.es”>Carlos Mateo</a>
 *
 * Date: 2019-11-08
 */

interface EmaViewUtils {

    fun checkVisibility(visibility: Boolean, gone: Boolean = true): Int {
        return when {
            visibility -> View.VISIBLE
            !visibility && gone -> View.GONE
            else -> View.INVISIBLE
        }
    }

    fun <T> checkUpdate(oldValue: T, newValue: T, action: (T) -> Unit) {
        if (oldValue != newValue)
            action.invoke(newValue)
    }

    fun getFormattedString(context:Context,@StringRes stringRes:Int,data:Any) :String{
        return String.format(context.getString(stringRes),data)
    }

    fun getFormattedString(string:String,data:Any) :String{
        return String.format(string,data)
    }
}