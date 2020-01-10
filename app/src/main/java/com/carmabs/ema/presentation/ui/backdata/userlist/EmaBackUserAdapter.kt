package com.carmabs.ema.presentation.ui.backdata.userlist

import android.view.View
import com.carmabs.ema.R
import com.carmabs.ema.android.ui.EmaRecyclerAdapter
import kotlinx.android.synthetic.main.item_back_user.view.*

/**
 * <p>
 * Copyright (c) 2019, Babel Sistemas de Información. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:carlos.mateo@babel.es”>Carlos Mateo</a>
 *
 * Date: 2019-11-07
 */

class EmaBackUserAdapter(override val listItems: MutableList<EmaBackUserModel> = mutableListOf()) : EmaRecyclerAdapter<EmaBackUserModel>() {

    override val layoutItemId: Int = R.layout.item_back_user

    override fun View.bind(item: EmaBackUserModel, viewType: Int) {
        tvItemUserName.text = item.name
        tvItemUserSurname.text = item.surname

    }
}