package com.carmabs.ema.core.extension

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.takeWhile
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

inline fun <reified R> Flow<R & Any>.whileIsInstanceOf(clazz: KClass<out R & Any>): Flow<R> {
    return takeWhile { it::class.isSubclassOf(clazz) }
}

suspend inline fun <reified R> Flow<R>.untilInstanceOf(clazz: KClass<out R&Any>): R {
    return first { it?.let { it::class.isSubclassOf(clazz) } ?: false }
}

suspend inline fun <T> Flow<T>.until(noinline condition: (T) -> Boolean): T {
    return first(condition)
}

fun <T> Flow<T>.concat(flow: Flow<T>): Flow<T> {
    return onCompletion { emitAll(flow) }
}
