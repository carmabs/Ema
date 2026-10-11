package com.carmabs.emanate.android.viewmodel

import androidx.annotation.CallSuper
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import com.carmabs.emanate.core.viewmodel.EmaViewModelContract

/**
 * Created by Carlos Mateo Benito on 13/02/2021.
 *
 * <p>
 * Copyright (c) 2021 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
open class EmaAndroidViewModel<S : EmaState, E : EmaEvent>(
    val emaViewModel: EmaViewModelContract<S, E>,
    val savedStateHandle: SavedStateHandle
) : ViewModel() {

    @CallSuper
    override fun onCleared() {
        emaViewModel.onCleared()
        super.onCleared()
    }
}
