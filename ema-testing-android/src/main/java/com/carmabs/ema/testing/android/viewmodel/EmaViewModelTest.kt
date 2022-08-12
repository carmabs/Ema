package com.carmabs.ema.testing.android.viewmodel

import com.carmabs.ema.core.viewmodel.EmaViewModel
import com.carmabs.ema.testing.core.EmaTest
import com.carmabs.ema.testing.core.concurrency.TestDefaultConcurrencyManager

/**
 * Class for testing a View Model. Provide the execution threads for asynchronous task to make the
 * code testable
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

abstract class EmaViewModelTest<VM : EmaViewModel<*, *>> : EmaTest(){

    protected val vm: VM by lazy {
        onSetupViewModel()
    }

    override fun setup() {
        super.setup()
        vm.concurrencyManager = TestDefaultConcurrencyManager()
    }

    abstract fun onSetupViewModel(): VM


}