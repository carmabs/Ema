package com.carmabs.data.manager

import android.content.Context
import com.carmabs.data.R
import com.carmabs.domain.manager.ResourceManager

/**
 * <p>
 * Copyright (c) 2019, Babel Sistemas de Información. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:carlos.mateo@babel.es”>Carlos Mateo</a>
 *
 * Date: 2019-11-08
 */

class AndroidResourceManager(private val context: Context) : ResourceManager {

    override fun getResultErrorFillName(): String {
        return context.getString(R.string.back_result_fill_name)
    }

    override fun getResultErrorFillSurname(): String {
        return context.getString(R.string.back_result_fill_surname)
    }
}