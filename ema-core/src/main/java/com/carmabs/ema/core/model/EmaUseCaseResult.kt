package com.carmabs.ema.core.model

import com.carmabs.ema.core.concurrency.ConcurrencyManager
import com.carmabs.ema.core.delegate.emaSyncDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Carlos Mateo Benito on 16/8/22.
 *
 * <p>
 * Copyright (c) 2022 by Carmabs. All rights reserved.
 * </p>
 * Class used  to handle the coroutine result with the methods and variables
 * - onSuccess when the result of action function is successful
 * - onError when the action function has thrown an error
 * - onFinish when the action function has ended, independently if an error has been thrown
 * - job returns the job where the action function has been executed+
 * @param concurrencyManager than launch the coroutine
 * @param fullException to use a job or supervisor job
 * @param onAction function that will be executed inside the concurrency manager
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */

class EmaUseCaseResult<T> internal constructor(
    private val concurrencyManager: ConcurrencyManager,
    fullException: Boolean,
    onAction: suspend CoroutineScope.() -> T
) {
    val job: Job

    private var result: EmaResult<T, Throwable>? by emaSyncDelegate(null)

    private var listenerList: MutableList<suspend (EmaResult<T, Throwable>) -> Unit> = Collections.synchronizedList(
        mutableListOf())

    private var finishList: MutableList<suspend () -> Unit> = Collections.synchronizedList(
        mutableListOf())

    private var finished: AtomicBoolean = AtomicBoolean(false)

    init {
        job = concurrencyManager.launch(fullException = fullException) {
            try {
                val data = onAction.invoke(this)
                result = EmaResult.success(data)

            } catch (e: Exception) {
                result = EmaResult.failure(e)

            } finally {
                result?.also { resultData->
                    listenerList.forEach {
                        it.invoke(resultData)
                    }
                }
                finished.set(true)
                finishList.forEach {
                    it.invoke()
                }
                listenerList.clear()
                finishList.clear()
            }
        }
    }

     fun onSuccess(successAction: suspend (T) -> Unit): EmaUseCaseResult<T> {
        (result as? EmaResult.Success)?.also {
            concurrencyManager.launch {
                successAction.invoke(it.data)
            }
        } ?: also {
            listenerList.add {
                it.onSuccess { data->
                    successAction.invoke(data)
                }

            }
        }
        return this
    }

    fun onError(errorAction: suspend (Throwable) -> Unit): EmaUseCaseResult<T> {
        (result as? EmaResult.Failure)?.also {
            concurrencyManager.launch {
                errorAction.invoke(it.failure)
            }
        } ?: also {
            listenerList.add {
                it.onFailure { exception->
                    errorAction.invoke(exception)
                }
            }
        }
        return this
    }

    fun onFinish(finishAction: suspend () -> Unit): EmaUseCaseResult<T> {
        if (finished.get())
            concurrencyManager.launch {
                finishAction.invoke()
            }
        else {
            finishList.add(finishAction)
        }
        return this
    }

}

