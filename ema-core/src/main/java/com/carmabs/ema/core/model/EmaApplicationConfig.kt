package com.carmabs.ema.core.model

import androidx.annotation.RestrictTo

/**
 * Created by Carlos Mateo Benito on 20/10/24.
 *
 * <p>
 * Copyright (c) 2024 by Carmabs. All rights reserved.
 * </p>
 *
 * Used to configure Ema library behaviour.
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object EmaApplicationConfigProvider{

    lateinit var instance:EmaApplicationConfig
        private set

    fun init(config:EmaApplicationConfig){
        if(::instance.isInitialized){
            throw UnsupportedOperationException("EmaApplicationConfig can be initialized only once")
        }
        instance = config
    }
}
class EmaApplicationConfig(
    val sideEffectConfig: EmaSideEffectConfig = EmaSideEffectConfig()
)