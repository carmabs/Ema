package com.carmabs.ema.presentation.base

import com.carmabs.ema.android.viewmodel.EmaViewModel
import com.carmabs.ema.core.navigator.EmaNavigationState

/**
 *  *<p>
 * Copyright (c) 2020, Carmabs. All rights reserved.
 * </p>
 *
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

abstract class BaseViewModel<S, NS : EmaNavigationState> : EmaViewModel<S, NS> (){

    override fun onResume(firstTime: Boolean) {
        //Override if you want to do some task everytime the view goes
        //to foreground
    }
}