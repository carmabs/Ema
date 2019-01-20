package com.carmabs.ema.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


/**
 * Interface which all factories of [EmaViewModel] must use to be handled by the library
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
interface EmaBaseFactory<VM> : ViewModelProvider.Factory{

    /**
     * Called if the view model has not been created yet
     * @param T View model class
     * @param modelClass Class of the view model
     */
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return createViewModel() as T
    }

    fun createViewModel():VM
}