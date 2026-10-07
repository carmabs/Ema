package com.carmabs.ema.core.extension

import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.takeWhile

inline fun <reified R> Flow<R & Any>.whileIsInstanceOf(clazz: KClass<out R & Any>): Flow<R> = takeWhile {
    clazz.isInstance(it)
}

suspend inline fun <reified R> Flow<R>.untilInstanceOf(clazz: KClass<out R&Any>): R = first { clazz.isInstance(it) }

suspend inline fun <T> Flow<T>.until(noinline condition: (T) -> Boolean): T = first(condition)

fun <T> Flow<T>.concat(flow: Flow<T>): Flow<T> = onCompletion { emitAll(flow) }
