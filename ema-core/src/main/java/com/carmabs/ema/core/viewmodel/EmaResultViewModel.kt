package com.carmabs.ema.core.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/**
 * Created by Carlos Mateo Benito on 2019-11-03.
 *
 * <p>
 * Copyright (c) 2019 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
class EmaResultViewModel(private val coroutineScope: CoroutineScope) {

    companion object {
        const val RESULT_ID_DEFAULT = -15834
    }

    private val resultMap: HashMap<Int, EmaResultModel> = HashMap()
    private val receiverMap: HashMap<Int, EmaReceiverModel> = HashMap()

    /**
     * Observable to notify result event when it is set up
     */
    val resultEvent: SharedFlow<EmaResultModel> = MutableSharedFlow()

    /**
     * Observable to notify result receiver invocation event when it is launched
     */
    val resultReceiverEvent: SharedFlow<EmaReceiverModel> = MutableSharedFlow()

    /**
     * Used for notify result data between views
     * @param emaResultModel notified
     */
    fun addResult(emaResultModel: EmaResultModel) {
        coroutineScope.launch {
            resultMap[emaResultModel.id] = emaResultModel
            (resultEvent as MutableSharedFlow).apply {
                emit(emaResultModel)
            }
        }
    }

    /**
     * Notify the results to all receivers are listening to them. The owner code is the id of
     * the class (viewmodel) is notifying, to avoid notify results that are not theirs
     */
    fun notifyResults(ownerCode: Int) {

        coroutineScope.launch {

            val keysToRemove = mutableListOf<Int>()

            //Map for avoid iteration exception if a result is added on receiver invocation
            val receiverExecutions = mutableListOf<() -> Unit>()

            resultMap.forEach {
                val data = it.value
                val key = it.key
                val ownerId = data.ownerId
                if (ownerCode == ownerId) {
                    val value = receiverMap[key]?.let { receiver ->
                        if (ownerCode != receiver.ownerCode) {
                            receiver.function.invoke(data)
                        }

                        (resultReceiverEvent as MutableSharedFlow).apply { emit(receiver) }
                        keysToRemove.add(key)
                    }

                    receiverExecutions.add { value }
                }
            }

            receiverExecutions.forEach { it.invoke() }

            keysToRemove.forEach {
                resultMap.remove(it)
                receiverMap.remove(it)
            }

            keysToRemove.clear()
        }
    }

    /**
     * Add listener when result is notified
     */
    fun addResultReceiver(receiver: EmaReceiverModel) {
        receiverMap[receiver.resultId] = receiver
    }

    fun onDestroy() {
        receiverMap.clear()
        resultMap.clear()
    }
}