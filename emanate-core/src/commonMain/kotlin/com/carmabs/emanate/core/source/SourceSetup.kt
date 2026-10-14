package com.carmabs.emanate.core.source

import com.carmabs.emanate.core.action.EmaAction
import com.carmabs.emanate.core.source.middleware.EmaMiddleware
import com.carmabs.emanate.core.source.reducer.EmaReducer
import com.carmabs.emanate.core.source.sideeffect.SideEffect
import com.carmabs.emanate.core.state.EmaEvent
import com.carmabs.emanate.core.state.EmaState
import kotlin.reflect.KClass

/**
 * Created by Carlos Mateo Benito on 12/10/2026.
 *
 * Middlewares, reducers, side effects and broadcast listeners of a source, and its installed plugins, added inside a
 * [SourceSetupScope].
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
class SourceSetup<S : EmaState, A : EmaAction, E : EmaEvent> internal constructor(
    internal val middlewares: List<EmaMiddleware<S, A, *>>,
    internal val reducers: List<EmaReducer<S, *>>,
    internal val sideEffects: List<SideEffect<S, A, E, *>>,
    internal val broadcastListeners: List<KClass<out EmaAction>>,
    internal val plugins: List<EmaSourcePlugin<S>>
)

/**
 * Runs [setup] and returns what it has added. Nothing can be added after it.
 */
internal fun <S : EmaState, A : EmaAction, E : EmaEvent> buildSourceSetup(
    setup: SourceSetupScope<S, A, E>.() -> Unit
): SourceSetup<S, A, E> = SourceSetupScope<S, A, E>().apply(setup).build()
