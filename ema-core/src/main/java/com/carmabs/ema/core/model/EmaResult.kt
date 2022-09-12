package com.carmabs.ema.core.model

/**
 * Created by Carlos Mateo Benito on 16/8/22.
 *
 * <p>
 * Copyright (c) 2022 by Carmabs. All rights reserved.
 * </p>
 * Improvement of Result that allows to set a failure value different than Throwable
 * It has some methods to handle the result and map the values.
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
sealed class EmaResult<T, E> private constructor(){
    class Success<T, E> internal constructor(val data: T) : EmaResult<T, E>()
    class Failure<T, E> internal constructor(val failure: E) : EmaResult<T, E>()

    companion object {
        fun <T, E> failure(error: E): EmaResult<T, E> = Failure(error)
        fun <T, E> success(data: T): EmaResult<T, E> = Success(data)
    }


    fun getFailureOrNull(): E? {
        return if (this is Failure) {
            failure
        } else
            null
    }


    fun getOrNull(): T? {
        return if (this is Success) {
            data
        } else
            null
    }

    fun getOrThrow(): T {
        return when (this) {
            is Failure -> throw (failure as? Throwable)
                ?: IllegalArgumentException(failure.toString())
            is Success -> data
        }
    }

    fun getOrDefault(default: T): T {
        return if (this is Success) {
            data
        } else
            default
    }

    val isFailure: Boolean
        get() {
            return this is Failure<*, *>
        }

    val isSuccess: Boolean
        get() {
            return this is Success<*, *>
        }


}

inline fun <T, E, R> EmaResult<T, E>.map(successMap: (T) -> R): EmaResult<R, E> {
    return when (this) {
        is EmaResult.Failure -> EmaResult.failure(this.failure)
        is EmaResult.Success -> EmaResult.success(successMap.invoke(data))
    }

}

inline fun <T, E, R> EmaResult<T, E>.flatMap(successMap: (T) -> EmaResult<R, E>): EmaResult<R, E> {
    return when (this) {
        is EmaResult.Failure -> {
            EmaResult.failure(this.failure)
        }
        is EmaResult.Success -> {
            successMap.invoke(this.data)
        }
    }
}

inline fun <T, E, R, ER> EmaResult<T, E>.mapResult(resultMap: (EmaResult<T, E>) -> EmaResult<R, ER>): EmaResult<R, ER> {
    return resultMap.invoke(this)
}

inline fun <T, E, ER> EmaResult<T, E>.flatMapError(errorMap: (E) -> EmaResult<T, ER>): EmaResult<T, ER> {
    return when (this) {
        is EmaResult.Failure -> {
            errorMap.invoke(this.failure)
        }
        is EmaResult.Success -> {
            EmaResult.success(this.data)
        }
    }
}


inline fun <T, E> EmaResult<T, E>.onSuccess(success: (T) -> Unit): EmaResult<T, E> {
    if (this is EmaResult.Success) {
        success.invoke(data)
    }
    return this
}

inline fun <T, E> EmaResult<T, E>.onFailure(error: (E) -> Unit): EmaResult<T, E> {
    if (this is EmaResult.Failure) {
        error.invoke(this.failure)
    }
    return this
}

inline fun <T, E, R> EmaResult<T, E>.mapError(errorMap: (E) -> R): EmaResult<T, R> {
    return when (this) {
        is EmaResult.Failure -> EmaResult.failure(errorMap.invoke(failure))
        is EmaResult.Success -> EmaResult.success(data)
    }

}
