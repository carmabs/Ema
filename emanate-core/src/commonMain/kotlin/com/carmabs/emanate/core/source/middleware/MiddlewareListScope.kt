package com.carmabs.emanate.core.source.middleware

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.source.EmaSourceDsl
import com.carmabs.emanate.core.source.SetupGuard
import com.carmabs.emanate.core.state.EmaState
import kotlin.reflect.KClass

/**
 * Created by Carlos Mateo Benito on 12/10/2026.
 *
 * Block of setup that adds the middlewares of the source: with [add], or with `+` for the ones already created. They
 * handle the actions in the order they are added, before the reducers.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
@EmaSourceDsl
class MiddlewareListScope<S : EmaState, A : EmaAction> internal constructor(private val guard: SetupGuard) {

    internal var middlewareList: List<EmaMiddleware<S, A, *>> = emptyList()
        private set

    /**
     * Adds middlewares already created.
     */
    fun add(vararg middleware: EmaMiddleware<S, A, *>) {
        guard.checkNotBuilt()
        middlewareList = middlewareList + middleware
    }

    /**
     * Adds a middleware already created.
     */
    operator fun EmaMiddleware<S, A, *>.unaryPlus() {
        add(this)
    }

    /**
     * Implementation of the inline add with the type of the actions.
     */
    @PublishedApi
    internal fun <F : EmaAction> add(filter: KClass<F>, middlewareAction: MiddlewareScope<S, A>.(F) -> Unit) {
        add(middlewareOf(filter, middlewareAction))
    }

    /**
     * Adds a middleware for the actions of type [F]. The other actions continue to the next middleware. Use EmaAction
     * to handle every action.
     */
    inline fun <reified F : EmaAction> add(noinline middlewareAction: MiddlewareScope<S, A>.(F) -> Unit) {
        add(F::class, middlewareAction)
    }
}
