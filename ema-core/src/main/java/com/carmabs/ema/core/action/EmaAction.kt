package com.carmabs.ema.core.action

/**
 * Created by Carlos Mateo Benito on 1/10/23.
 *
 * <p>
 * Copyright (c) 2023 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
sealed interface EmaAction{

    companion object{
        val type: String = EmaAction::class.java.simpleName
    }
    val type: String
        get() = type
}
