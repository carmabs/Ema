package com.carmabs.emanate.core.source.middleware

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import com.carmabs.emanate.core.viewmodel.EmaViewModel
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlinx.coroutines.delay

/**
 * Created by Carlos Mateo Benito on 11/10/2026.
 *
 * Middleware that delays the actions of type [F] before they continue the chain.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
abstract class EmaMiddlewareDelay<S : EmaState, A : EmaAction, F : EmaAction>(
    final override val actionType: KClass<F>,
    private val delay: Duration
) : EmaMiddleware<S, A, F> {

    final override fun MiddlewareScope<S, A>.invoke(action: F) {
        nextAfter {
            delay(delay)
            onActionDelayed(action)
        }
    }

    /**
     * Called when the delay finishes, before the action continues. Call [NextAfterScope.stop] to stop it.
     */
    open suspend fun NextAfterScope<S, A>.onActionDelayed(action: F) = Unit
}

fun <S : EmaState, A : EmaAction, F : EmaAction> middlewareDelayOf(
    actionType: KClass<F>,
    delay: Duration,
    onActionDelayed: (NextAfterScope<S, A>.(F) -> Unit)? = null
): EmaMiddlewareDelay<S, A, F> = object : EmaMiddlewareDelay<S, A, F>(actionType, delay) {

    override suspend fun NextAfterScope<S, A>.onActionDelayed(action: F) {
        onActionDelayed?.invoke(this, action)
    }
}

fun <S : EmaState, A : EmaAction, F : EmaAction> MiddlewareListScope<S, A>.middlewareDelayOf(
    actionType: KClass<F>,
    delay: Duration,
    onActionDelayed: (NextAfterScope<S, A>.(F) -> Unit)? = null
): EmaMiddlewareDelay<S, A, F> =
    com.carmabs.emanate.core.source.middleware.middlewareDelayOf(actionType, delay, onActionDelayed)

fun <S : EmaState, A : EmaAction, E : EmaEvent, F : EmaAction> EmaViewModel<S, A, E>.middlewareDelayOf(
    actionType: KClass<F>,
    delay: Duration,
    onActionDelayed: (NextAfterScope<S, A>.(F) -> Unit)? = null
): EmaMiddlewareDelay<S, A, F> =
    com.carmabs.emanate.core.source.middleware.middlewareDelayOf(actionType, delay, onActionDelayed)
