package com.carmabs.ema.compose.action

import androidx.compose.runtime.Immutable
import com.carmabs.ema.core.action.EmaAction
import com.carmabs.ema.core.action.EmaActionDispatcher
import kotlinx.coroutines.flow.Flow

/**
 * Created by Carlos Mateo Benito on 16/7/23.
 *
 * <p>
 * Copyright (c) 2023 by Carmabs. All rights reserved.
 * </p>
 *
 * Variation of EmaActionDispatcher to improve composables recomposition performance.
 * Make it immutable, guarantees that listeners based on actions are skippables so recomposition is avoided.
 * Otherwise recomposition is launched everytime a listener has inside its implementation and outer params considered
 * unstable
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
@Immutable
interface EmaImmutableActionDispatcher<in A : EmaAction> : EmaActionDispatcher<A> {

    /**
     * Dispatcher that ignores every action, for example in previews. It works with any type of action.
     */
    data object EMPTY : EmaImmutableActionDispatcher<EmaAction> {
        override fun dispatch(action: EmaAction) = Unit
    }
}

fun <A : EmaAction> EmaActionDispatcher<A>.toImmutable(): EmaImmutableActionDispatcher<A> =
    object : EmaImmutableActionDispatcher<A> {
        override fun dispatch(action: A) {
            this@toImmutable.dispatch(action)
        }
    }
