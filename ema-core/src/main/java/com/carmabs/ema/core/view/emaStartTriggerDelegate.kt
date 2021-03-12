package com.carmabs.ema.core.view

import kotlin.reflect.KProperty

/**
 * Created by Carlos Mateo Benito on 09/03/2021.
 *
 * <p>
 * Copyright (c) 2021 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
class emaStartTriggerDelegate {

    val startTrigger: EmaViewModelTrigger = EmaViewModelTrigger()

    operator fun getValue(thisRef: Any?, property: KProperty<*>): EmaViewModelTrigger {
        return startTrigger
    }
}