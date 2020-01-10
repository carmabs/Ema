package com.carmabs.ema.android.ui

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
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

    companion object {
        const val RESULT_ID_DEFAULT = -15834
    }

    private val resultMap: HashMap<Int, EmaResultModel> = HashMap()
    private val receiverMap: HashMap<Int, EmaReceiverModel> = HashMap()

    /**
     * Observable to notify result event when it is set up
     */
    val resultEvent: LiveData<EmaResultModel> = MutableLiveData()

    /**
     * Observable to notify result receiver invocation event when it is launched
     */
    val resultReceiverEvent: LiveData<EmaReceiverModel> = MutableLiveData()

    /**
     * Used for notify result data between views
     * @param emaResultModel notified
     */
    internal fun addResult(emaResultModel: EmaResultModel) {
        resultMap[emaResultModel.id] = emaResultModel
        (resultEvent as MutableLiveData).apply { value = emaResultModel }
    }

    /**
     * Notify the results to all receivers are listening to them. The owner code is the id of
     * the class (viewmodel) is notifying, to avoid notify results that are not theirs
     */
    internal fun notifyResults(ownerCode: Int) {

        val keysToRemove = mutableListOf<Int>()

        //Map for avoid iteration exception if a result is added on receiver invocation
        val receiverExecutions = mutableListOf<()->Unit>()

        resultMap.forEach {
            val data = it.value
            val key = it.key
            val ownerId = data.ownerId
            if (ownerCode == ownerId) {
                receiverExecutions.add {
                    receiverMap[key]?.let { receiver ->
                        if (ownerCode != receiver.ownerCode) {
                            receiver.function.invoke(data)
                        }
                        (resultReceiverEvent as MutableLiveData).apply { value = receiver }
                        keysToRemove.add(key)
                    }
                }
            }
        }

        receiverExecutions.forEach { it.invoke() }

        keysToRemove.forEach {
            resultMap.remove(it)
            receiverMap.remove(it)
        }

        keysToRemove.clear()
    }

    /**
     * Add listener when result is notified
     */
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