package com.carmabs.ema.android.base

/**
 * Class to wrap the  view model seed for first instance creation.
 *
 * @author <a href="mailto:apps.carmabs@gmail.com">Carlos Mateo Benito</a>
 */
class EmaFactory<VM>(val viewModelSeed: VM) : EmaBaseFactory<VM> {

    /**
     * @return View model instance
     */
    override fun createViewModel(): VM {
        return viewModelSeed
    }
}
