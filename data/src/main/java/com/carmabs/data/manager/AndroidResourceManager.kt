package com.carmabs.data.manager

import android.content.Context
import com.carmabs.data.R
import com.carmabs.domain.manager.ResourceManager

/**
 *<p>
 * Copyright (c) 2020, Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
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

    override fun getHomeUserToolbarTitle(): String {
        return context.getString(R.string.user_toolbar_title)
    }

    override fun getCongratulations(): String {
        return context.getString(R.string.home_congratulations)
    }

    override fun getNumberPeople(number: Int): String {
        return String.format(context.getString(R.string.user_number_people),number)
    }
}