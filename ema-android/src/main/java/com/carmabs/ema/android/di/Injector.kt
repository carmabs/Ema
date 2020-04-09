package com.carmabs.ema.android.di

import org.kodein.di.Kodein
import org.kodein.di.KodeinAware

/**
 * Created by Carlos Mateo Benito on 2020-03-09.
 *
 * <p>
 * Copyright (c) 2020 by atSistemas. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:cmateo.benito@atsistemas.com”>Carlos Mateo Benito</a>
 */
interface Injector  : KodeinAware {

    val parentKodein: Kodein

    override val kodein: Kodein
        get() = Kodein.lazy {
            extend(parentKodein)
            injectModule(this)?.let {
                import(it)
            }
        }

    fun injectModule(kodeinBuilder: Kodein.MainBuilder):Kodein.Module?
}