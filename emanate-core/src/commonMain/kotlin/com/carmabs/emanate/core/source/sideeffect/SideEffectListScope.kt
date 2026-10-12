package com.carmabs.emanate.core.source.sideeffect

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.source.EmaSourceDsl
import com.carmabs.emanate.core.source.SetupGuard
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import kotlin.reflect.KClass
import kotlin.time.Duration

/**
 * Created by Carlos Mateo Benito on 12/10/2026.
 *
 * Block of setup that adds the side effects of the source: with [add], or with `+` for the ones already created. Each
 * one runs asynchronously after the reducers of the actions of its type.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
@EmaSourceDsl
class SideEffectListScope<S : EmaState, A : EmaAction, E : EmaEvent> internal constructor(
    private val guard: SetupGuard
) {

    internal var sideEffectList: List<SideEffect<S, A, E, *>> = emptyList()
        private set

    /**
     * Adds side effects already created.
     */
    fun add(vararg sideEffect: SideEffect<S, A, E, *>) {
        guard.checkNotBuilt()
        sideEffectList = sideEffectList + sideEffect
    }

    /**
     * Adds a side effect already created.
     */
    operator fun SideEffect<S, A, E, *>.unaryPlus() {
        add(this)
    }

    /**
     * Implementation of the inline add with the type of the actions.
     */
    @PublishedApi
    internal fun <F : EmaAction> add(
        filter: KClass<F>,
        concurrency: SideEffectConcurrency,
        timeout: Duration?,
        onTimeout: (SideEffectScope<S, A, E>.(F) -> Unit)?,
        sideEffect: suspend SideEffectScope<S, A, E>.(F) -> Unit
    ) {
        add(sideEffectOf(filter, concurrency, timeout, onTimeout, sideEffect))
    }

    /**
     * Adds a side effect for the actions of type [F]. It runs asynchronously after their reducers.
     * @param concurrency what happens when an action launches it while it is still running
     * @param timeout if the side effect doesn't finish in this time, it is cancelled and [onTimeout] is called
     */
    inline fun <reified F : EmaAction> add(
        concurrency: SideEffectConcurrency = SideEffectConcurrency.Every,
        timeout: Duration? = null,
        noinline onTimeout: (SideEffectScope<S, A, E>.(F) -> Unit)? = null,
        noinline sideEffect: suspend SideEffectScope<S, A, E>.(F) -> Unit
    ) {
        add(F::class, concurrency, timeout, onTimeout, sideEffect)
    }
}
