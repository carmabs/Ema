package com.carmabs.ema.android.viewmodel

import androidx.annotation.CallSuper
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carmabs.ema.core.state.EmaEvent
import com.carmabs.ema.core.state.EmaState
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
open class EmaAndroidViewModel<S:EmaState,E: EmaEvent>(
    val emaViewModel:EmaViewModel<S,E>,
    val savedStateHandle: SavedStateHandle
) : ViewModel() {

    init {
        emaViewModel.setScope(viewModelScope)
    }
    @CallSuper
    override fun onCleared() {
       emaViewModel.onCleared()
        super.onCleared()
    }
}
