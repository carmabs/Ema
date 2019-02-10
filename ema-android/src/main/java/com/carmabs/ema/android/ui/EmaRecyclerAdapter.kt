package com.carmabs.ema.android.ui

import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter to implement the list view interface in recycler views
 *
 * <p>
 * Copyright (c) 2019, Babel Sistemas de Información. All rights reserved.
 * </p>
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */

abstract class EmaRecyclerAdapter<I> : RecyclerView.Adapter<EmaViewHolder<I>>() {

    /**
     * Items to show in list
     */
    abstract val listItems:MutableList<I>

    /**
     * Get the list size
     */
    override fun getItemCount(): Int {
        return listItems.size
    }

    /**
     * Method called to implement the view data set up
     * @param holder holder where view components are implemented
     * @param position current item position in recycler view
     */
    override fun onBindViewHolder(holder: EmaViewHolder<I>, position: Int) {
        holder.bind(listItems[position])
    }

    /**
     * Update the adapter with a new list
     * @param listUpdate to set in the adapter
     */
    fun updateList(listUpdate:List<I>){
        listItems.apply {
            clear()
            addAll(listUpdate)
            notifyDataSetChanged()
        }
    }

    /**
     * Update the item of the list if exists. It must have the same memory reference
     * @param item to update
     */
    fun updateItem(item : I){
        val index = listItems.indexOf(item)
        if(index>-1){
            listItems[index] = item
            notifyItemChanged(index)
        }
    }
}