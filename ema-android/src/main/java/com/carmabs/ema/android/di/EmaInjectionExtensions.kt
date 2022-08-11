package com.carmabs.ema.android.di

import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.java.KoinJavaComponent

/**
 * Created by Carlos Mateo Benito on 10/8/22.
 *
 * <p>
 * Copyright (c) 2022 by Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 */
inline fun <reified T> AndroidScopeComponent.injectDirect(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): T {
    return scope.get(T::class, qualifier = qualifier, parameters = parameters)
}

inline fun <reified T> injectDirect(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
) = KoinJavaComponent.getKoin().get<T>(T::class, qualifier = qualifier, parameters = parameters)
