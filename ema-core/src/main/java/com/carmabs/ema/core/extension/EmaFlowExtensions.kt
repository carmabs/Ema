package com.carmabs.ema.core.extension

import com.carmabs.ema.core.model.EmaEvent
import com.carmabs.ema.core.navigator.EmaNavigationDirectionEvent
import com.carmabs.ema.core.navigator.EmaNavigationEvent
import com.carmabs.ema.core.state.EmaDataState
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.core.state.EmaState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlin.reflect.jvm.jvmName

suspend inline fun <T, reified R> Flow<T>.untilInstanceOf(clazz: Class<R>): R {
    return first { it is R } as R
}

suspend inline fun <T> Flow<T>.until(noinline condition: (T) -> Boolean): T {
    return first(condition)
}

fun <T> Flow<T>.concat(flow: Flow<T>): Flow<T> {
    return onCompletion { emitAll(flow) }
}

fun <S : EmaDataState, N : EmaNavigationEvent> Flow<EmaState<S, N>>.distinctStateDataChanges(): Flow<EmaState<S, N>> {
    return distinctUntilChanged { old, new ->
        when {
            old.data != new.data -> false
            old::class.jvmName != new::class.jvmName -> false
            old is EmaState.Overlapped && new is EmaState.Overlapped
                    && old.extraData != new.extraData ->  false
            else -> true
        }
    }
}

fun <S : EmaDataState, N : EmaNavigationEvent> Flow<EmaState<S, N>>.distinctNavigationChanges(): Flow<EmaNavigationDirectionEvent> {
    return distinctUntilChanged { old, new ->
        old.navigation == new.navigation
    }.map {
        it.navigation
    }
}

fun <S : EmaDataState, N : EmaNavigationEvent> Flow<EmaState<S, N>>.distinctSingleEventChanges(): Flow<EmaEvent> {
    return distinctUntilChanged { old, new ->
        old.singleEvent == new.singleEvent
    }.map {
        it.singleEvent
    }
}