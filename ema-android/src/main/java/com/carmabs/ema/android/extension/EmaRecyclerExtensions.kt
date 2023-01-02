package com.carmabs.ema.android.extension

import androidx.recyclerview.widget.RecyclerView

/**
 * Extensions for recycler
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

/**
 * Clear the adapters to avoid memory leaks
 */
fun RecyclerView.clearAdapters(){
    adapter = null
}
