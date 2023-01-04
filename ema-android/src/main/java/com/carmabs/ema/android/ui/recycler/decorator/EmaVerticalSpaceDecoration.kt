package com.carmabs.ema.android.ui.recycler.decorator

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Carlos Mateo Benito on 2020-04-14.
 *
 * <p>
 * Copyright (c) 2020 by atSistemas. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:cmateo.benito@atsistemas.com”>Carlos Mateo Benito</a>
 */
class EmaVerticalSpaceDecoration(
    private val verticalSpaceHeight: Int,
    private val addSpaceBelowLastItem:Boolean = false) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        when{
            addSpaceBelowLastItem -> {
                outRect.bottom = verticalSpaceHeight
            }
            (parent.getChildAdapterPosition(view) != parent.adapter?.itemCount?.minus(1) ) -> {
                outRect.bottom = verticalSpaceHeight
            }
        }
    }
}