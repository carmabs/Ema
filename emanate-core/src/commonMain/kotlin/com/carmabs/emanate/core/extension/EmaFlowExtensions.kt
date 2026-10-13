package com.carmabs.emanate.core.extension

import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.takeWhile

/**
 * Emits the values while they are instances of [clazz]. The flow completes with the first value that is not.
 */
inline fun <reified R> Flow<R & Any>.whileIsInstanceOf(clazz: KClass<out R & Any>): Flow<R> = takeWhile {
    clazz.isInstance(it)
}

/**
 * Suspends until the flow emits an instance of [clazz] and returns it.
 */
suspend inline fun <reified R> Flow<R>.untilInstanceOf(clazz: KClass<out R&Any>): R = first { clazz.isInstance(it) }

/**
 * Suspends until the flow emits a value that meets [condition] and returns it.
 */
suspend inline fun <T> Flow<T>.until(noinline condition: (T) -> Boolean): T = first(condition)

/**
 * Emits the values of this flow and, when it completes, the ones of [flow].
 */
fun <T> Flow<T>.concat(flow: Flow<T>): Flow<T> = onCompletion { emitAll(flow) }
