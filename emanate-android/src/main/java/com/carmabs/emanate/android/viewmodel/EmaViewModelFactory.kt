package com.carmabs.emanate.android.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.carmabs.emanate.core.viewmodel.EmaViewModelContract

/**
 * Class to wrap the  view model seed for first instance creation.
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
class EmaViewModelFactory<VM : EmaViewModelContract<*, *>>(
    private val viewModel: VM,
    private val savedStateHandle: SavedStateHandle? = null
) : EmaFactory<VM>() {
    /**
     * @return View model instance
     */
    override fun createViewModel(): VM = viewModel

    override fun provideSavedStateHandle(): SavedStateHandle? = savedStateHandle
}
