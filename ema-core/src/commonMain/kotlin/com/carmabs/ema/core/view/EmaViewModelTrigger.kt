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

    fun startViewModel() {
        triggerAction?.invoke()
        hasBeenStarted = true
    }

    var hasBeenStarted = false
        private set
    internal var triggerAction: (() -> Unit)? = null
}