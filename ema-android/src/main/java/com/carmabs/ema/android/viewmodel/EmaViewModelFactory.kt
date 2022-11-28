package com.carmabs.ema.android.viewmodel

/**
 * Class to wrap the  view model seed for first instance creation.
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
class EmaViewModelFactory<VM>(private val viewModelSeed: VM) : EmaFactory<VM> {
    /**
     * @return View model instance
     */
    override fun createViewModel(): VM {
        return viewModelSeed
    }
}
