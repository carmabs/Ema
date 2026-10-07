package com.carmabs.ema.core.view

/**
 * Created by Carlos Mateo Benito on 09/03/2021.
 *
 * <p>
 * Copyright (c) 2021 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
class EmaViewModelTrigger {

    private val pendingActions = mutableListOf<() -> Unit>()

    var hasBeenStarted = false
        private set

    fun startViewModel() {
        hasBeenStarted = true
        val actions = pendingActions.toList()
        pendingActions.clear()
        actions.forEach { it.invoke() }
    }

    /**
     * Runs the action now if the ViewModel has been started, or when [startViewModel] is called otherwise.
     */
    internal fun runWhenStarted(action: () -> Unit) {
        if (hasBeenStarted)
            action.invoke()
        else
            pendingActions.add(action)
    }
}
