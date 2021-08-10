package com.carmabs.ema.android.di

import org.kodein.di.DI
import org.kodein.di.DIAware

/**
 * Created by Carlos Mateo Benito on 2020-03-09.
 *
 * <p>
 * Copyright (c) 2020 by atSistemas. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:cmateo.benito@atsistemas.com”>Carlos Mateo Benito</a>
 */
interface Injector  : DIAware {

    val parentKodein: DI

    override val di: DI

    fun injectModule(kodeinBuilder: DI.MainBuilder):DI.Module?

    fun injectKodein() =  DI.lazy {
            extend(parentKodein)
            injectModule(this)?.let {
                import(it)
            }
    }

}