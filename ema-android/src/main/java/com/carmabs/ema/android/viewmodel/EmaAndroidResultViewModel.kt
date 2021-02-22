package com.carmabs.ema.android.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.carmabs.ema.core.viewmodel.EmaResultModel
import com.carmabs.ema.core.viewmodel.EmaResultViewModel

/**
 * Created by Carlos Mateo Benito on 2019-11-03.
 *
 * <p>
 * Copyright (c) 2019 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
class EmaAndroidResultViewModel(val emaResultViewModel: EmaResultViewModel): ViewModel() {

    val resultReceiverEvent = emaResultViewModel.resultReceiverEvent.asLiveData()

    val resultEvent = emaResultViewModel.resultEvent.asLiveData()

    override fun onCleared() {
        super.onCleared()
        emaResultViewModel.onDestroy()
    }

    fun unBindObservables(owner: LifecycleOwner) {
        resultEvent.removeObservers(owner)
        resultReceiverEvent.removeObservers(owner)
    }

    fun addResult(resultModel: EmaResultModel) {
        emaResultViewModel.addResult(resultModel)
    }

    fun notifyResults(ownerId: Int) {
        emaResultViewModel.notifyResults(ownerId)
    }
}