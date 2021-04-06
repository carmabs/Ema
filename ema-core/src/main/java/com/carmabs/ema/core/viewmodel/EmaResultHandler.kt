package com.carmabs.ema.core.viewmodel

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Created by Carlos Mateo Benito on 2019-11-03.
 *
 * <p>
 * Copyright (c) 2019 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
class EmaResultHandler private constructor() {


    companion object {
        const val RESULT_ID_DEFAULT = 15834
        private val emaResultHandler = EmaResultHandler()

        fun getInstance():EmaResultHandler = emaResultHandler
    }

    private val resultMap: HashMap<Int, EmaResultModel> = HashMap()
    private val receiverMap: HashMap<Int, EmaReceiverModel> = HashMap()

    /**
     * Observable to notify result receiver invocation event when it is launched
     */
    val resultReceiverEvent: SharedFlow<EmaReceiverModel> = MutableSharedFlow()

    /**
     * Used for notify result data between views
     * @param emaResultModel notified
     */
    fun addResult(emaResultModel: EmaResultModel) {
        resultMap[emaResultModel.code] = emaResultModel
    }

    /**
     * Notify the results to all receivers are listening to them. The owner code is the id of
     * the class (viewmodel) is notifying, to avoid notify results that are not theirs
     */
    fun notifyResults(ownerId: Int) {
        val keysToRemove = mutableListOf<Int>()

        //Map for avoid iteration exception if a result is added on receiver invocation
        val receiverExecutions = mutableListOf<() -> Unit>()

        resultMap.forEach {
            val data = it.value
            val key = it.key
            val dataOwnerId = data.ownerId

            if (ownerId == dataOwnerId) {
                receiverMap[key]?.also { receiver ->
                    if (ownerId != receiver.ownerId) {
                        receiverExecutions.add {
                            receiver.function.invoke(data)
                        }
                    }
                    keysToRemove.add(key)
                }
            }
        }

        keysToRemove.forEach {
            resultMap.remove(it)
            receiverMap.remove(it)
        }

        receiverExecutions.forEach {
            it.invoke()
        }

        keysToRemove.clear()
    }

    /**
     * Add listener when result is notified
     */
    fun addResultReceiver(receiver: EmaReceiverModel) {
        receiverMap[receiver.resultCode] = receiver
    }

    fun onDestroy() {
        receiverMap.clear()
        resultMap.clear()
    }
}