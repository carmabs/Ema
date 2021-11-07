package com.carmabs.ema.android.ui


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.lang.RuntimeException
import java.util.*

/**
 * Adapter to implement the list view interface in recycler views
 *

 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */

abstract class EmaRecyclerAdapter<I> : RecyclerView.Adapter<EmaViewHolder<I>>() {

    /**
     * Items to show in list
     */
    protected val listItems: MutableList<I> = mutableListOf()

    /**
     * Listener when item is clicked
     */
    private var itemClickListener: ((index:Int,item: I) -> Unit)? = null

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
        holder.bind(listItems[position],position,listItems.size)
    }


    /**
     * Method called to set the ViewHolder. If different view holders are provided depending [viewType], use enableMultiViewHolder function
     * @param parent holder where view components are implemented
     * @param viewType of the item
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmaViewHolder<I> {
        return enableMultiViewHolder?.invoke(parent,viewType)
                ?: EmaAdapterViewHolder(LayoutInflater.from(parent.context).inflate(layoutItemId
                        ?: throw RuntimeException("Please provide the id for layoutItemId"), parent, false), viewType)
    }

    /**
     * Resource id for item layout
     */
    protected abstract val layoutItemId: Int?

    /**
     * Method to set the view parameters from each item
     */
    protected abstract fun View.bind(item: I, viewType: Int,position: Int,size: Int)

    /**
     * Function to implement different viewHolders depending the viewType provided. Use only if you want to use different
     * views depending the item provided
     */
    protected open val enableMultiViewHolder: ((view:ViewGroup,viewType:Int) -> EmaAdapterViewHolder)? = null

    /**
     * Update the adapter with a new list
     * @param listUpdate to set in the adapter
     */
    open fun updateList(listUpdate: List<I>) {
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
    open fun updateItem(item: I) {
        val index = listItems.indexOf(item)
        if (index > -1) {
            listItems[index] = item
            notifyItemChanged(index)
        }
    }

    /**
     * Add listener when item is clicked
     * @param listener for click on view item
     */
    open fun setOnItemClickListener(itemClickListener: ((index:Int,item: I) -> Unit)?) {
        this.itemClickListener = itemClickListener
    }

    /**
     * Add item to the list
     * @param item to add
     */
    open fun addItem(item: I, position: Int = listItems.size) {
        listItems.add(position, item)
    }

    protected open inner class EmaAdapterViewHolder(view: View, private val viewType: Int) : EmaViewHolder<I>(view) {
        override fun bind(item: I,position: Int,size:Int) {
            itemView.bind(item, viewType,position,size)
            itemView.setOnClickListener {
                onItemClicked(item,position,size)
                itemClickListener?.invoke(position,item)
            }
        }
    }

    protected open fun onItemClicked(item: I,position:Int,size:Int) {
        //Override to add internal logic when item is clicked
    }
}