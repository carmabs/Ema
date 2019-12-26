package com.carmabs.ema.android.ui

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.carmabs.ema.android.extra.EmaReceiverModel
import com.carmabs.ema.android.extra.EmaResultModel

/**
 * Created by Carlos Mateo Benito on 2019-11-03.
 *
 * <p>
 * Copyright (c) 2019 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
class EmaResultViewModel : ViewModel() {

    private val resultMap: HashMap<Int, EmaResultModel> = HashMap()
    private val receiverMap: HashMap<Int, EmaReceiverModel> = HashMap()

    /**
     * Observable to notify result data between different screens
     */
    val resultEvent: MutableLiveData<EmaResultModel> = MutableLiveData()

    /**
     * Used for notify result data between views
     * @param result notified
     */

    internal fun setResult(emaResultModel: EmaResultModel) {
        resultMap[emaResultModel.id] = emaResultModel
        resultEvent.value = emaResultModel
    }

    internal fun notifyResults(ownerCode: Int) {
        val keysToRemove = mutableListOf<Int>()
        resultMap.forEach {
            val data = it.value
            val key = it.key
            receiverMap[key]?.let { receiver ->
                if (ownerCode != receiver.ownerCode) {
                    receiver.function.invoke(data)
                    keysToRemove.add(key)
                }
            }
        }

        keysToRemove.forEach {
            resultMap.remove(it)
            receiverMap.remove(it)
        }

        keysToRemove.clear()
    }

    internal fun removeResultReceiver(code:Int){
        receiverMap.remove(code)
    }

    fun addResultReceiver(receiver: EmaReceiverModel) {
        receiverMap[receiver.resultId] = receiver
    }

    internal fun notifyResult(ownerCode: Int, emaResultModel: EmaResultModel) {
        receiverMap[emaResultModel.id]?.also { receiver ->
            if (ownerCode != receiver.ownerCode) {
                receiver.function.invoke(emaResultModel)
                resultMap.remove(emaResultModel.id)
                receiverMap.remove(emaResultModel.id)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        receiverMap.clear()
        resultMap.clear()
    }

    fun unBindObservables(owner: LifecycleOwner) {
        resultEvent.removeObservers(owner)
    }
}