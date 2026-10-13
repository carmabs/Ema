package com.carmabs.emanate.android.viewmodel

import androidx.annotation.CallSuper
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.carmabs.emanate.android.savestate.SavedStateHandleStore
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import com.carmabs.emanate.core.viewmodel.EmaViewModel
import com.carmabs.emanate.core.viewmodel.EmaViewModelContract

/**
 * Created by Carlos Mateo Benito on 13/02/2021.
 *
 * Android view model that keeps [emaViewModel] while its screen lives, configuration changes included. An
 * EmaViewModel saves the values added with saved in its setup in [savedStateHandle], and restores them after the
 * system kills the process of the app, both in Compose and in Views.
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

    init {
        (emaViewModel as? EmaViewModel<*, *, *>)?.attachStateStore(SavedStateHandleStore(savedStateHandle))
    }

    @CallSuper
    override fun onCleared() {
        emaViewModel.onCleared()
        super.onCleared()
    }
}
