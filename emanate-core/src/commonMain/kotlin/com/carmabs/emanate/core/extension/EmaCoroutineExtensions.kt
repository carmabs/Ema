package com.carmabs.emanate.core.extension

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope

/**
 * Creates a [CoroutineScope] with this context. If the context has no Job, the scope creates one.
 */
fun CoroutineContext.toScope() = CoroutineScope(this)
