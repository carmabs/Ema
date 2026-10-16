package com.carmabs.emanate.core.source.middleware

/**
 * Created by Carlos Mateo Benito on 16/10/2026.
 *
 * Place in the chain of the source of the middlewares added in a block, whatever the place where the block is
 * written. Inside each position, the middlewares handle the actions in the order they are added.
 *
 * <p>
 * Copyright (c) 2026 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
enum class MiddlewarePosition {

    /**
     * Before the rest: to decide the path of every action before anything else, like a session guard.
     */
    First,

    /**
     * Where the block is written, in order with the rest of the setup.
     */
    InPlace,

    /**
     * After the rest, right before the reducers: to observe the new state of every action, like a backup. When next
     * returns, the reducers of the action have run. It should call next, so the middlewares after it also see the new
     * state: to stop or delay the actions, use [First].
     */
    Last
}
