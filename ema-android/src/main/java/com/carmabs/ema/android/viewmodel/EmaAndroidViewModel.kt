package com.carmabs.ema.android.viewmodel

import androidx.lifecycle.ViewModel
import com.carmabs.ema.core.viewmodel.EmaViewModel

/**
 * Created by Carlos Mateo Benito on 13/02/2021.
 *
 * <p>
 * Copyright (c) 2021 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
abstract class EmaAndroidViewModel<VM : EmaViewModel<*,*>>(val emaViewModel:VM) :
    ViewModel() {

    val observableState = emaViewModel.getObservableState()

    override fun onCleared() {
        emaViewModel.onDestroy()
        super.onCleared()
    }
}