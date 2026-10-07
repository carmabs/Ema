@file:OptIn(ExperimentalAtomicApi::class)

package com.carmabs.ema.core.navigator

import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Generates a unique id for every node. A timestamp is not enough: two nodes created in the same
 * millisecond would share it.
 */
private val idGenerator = AtomicLong(0)

/**
 * Created by Carlos Mateo Benito on 25/5/23.
 *
 * <p>
 * Copyright (c) 2023 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */
data class EmaNavigationNode<T>(
    val value: T,
    private var previousNode: EmaNavigationNode<T>? = null,
) {
    val id: Long = idGenerator.addAndFetch(1L)
    val previous: EmaNavigationNode<T>?
        get() = previousNode

    fun next(node: T, singleTop: Boolean = false): EmaNavigationNode<T> {
        return EmaNavigationNode(
            value = node,
            previousNode = if (singleTop) {
                val origin = this
                skipNodeInNavigationHistoric(node, origin)
                origin
            } else
                this
        )
    }

    private fun skipNodeInNavigationHistoric(
        node: T,
        origin: EmaNavigationNode<T>?
    ) {
        when {
            origin == null ->
                return

            origin.value == node -> {
                skipNodeInNavigationHistoric(node, origin.previous)
            }

            origin.previous?.value == node -> {
                origin.previousNode = origin.previousNode?.previous
                skipNodeInNavigationHistoric(node, origin.previous)

            }

            else -> {
                skipNodeInNavigationHistoric(node, origin.previous)
            }
        }
    }

    fun back(): EmaNavigationNode<T>? {
        return previous
    }

    fun hasPreviousNode(node: EmaNavigationNode<T>): Boolean {
        return previous?.let {
            if (it.id == node.id)
                true
            else
                it.hasPreviousNode(node)
        } ?: false
    }

    fun hasPreviousNodeValue(value: T): Boolean {
        return previous?.let {
            if (it.value == value)
                true
            else
                it.hasPreviousNodeValue(value)
        } ?: false
    }
}

