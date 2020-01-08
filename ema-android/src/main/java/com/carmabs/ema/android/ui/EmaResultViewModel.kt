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

    companion object{
        const val RESULT_ID_DEFAULT = -1
    }

    private val resultMap: HashMap<Int, EmaResultModel> = HashMap()
    private val receiverMap: HashMap<Int, EmaReceiverModel> = HashMap()

    /**
     * Observable to notify result event when it is set up
     */
    val resultEvent: MutableLiveData<EmaResultModel> = MutableLiveData()

    /**
     * Observable to notify result receiver invocation event when it is launched
     */
    val resultReceiverEvent: MutableLiveData<EmaReceiverModel> = MutableLiveData()

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
            val ownerId = data.ownerId
            if(ownerCode == ownerId ) {
                receiverMap[key]?.let { receiver ->
                    if (ownerCode != receiver.ownerCode) {
                        receiver.function.invoke(data)
                    }
                    resultReceiverEvent.value = receiver
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

    internal fun removeResultReceiver(code:Int = RESULT_ID_DEFAULT){
        receiverMap.remove(code)
    }

    fun addResultReceiver(receiver: EmaReceiverModel) {
        receiverMap[receiver.resultId] = receiver
    }

    override fun onCleared() {
        super.onCleared()
        receiverMap.clear()
        resultMap.clear()
    }

    fun unBindObservables(owner: LifecycleOwner) {
        resultEvent.removeObservers(owner)
        resultReceiverEvent.removeObservers(owner)
    }
}