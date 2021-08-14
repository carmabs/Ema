package com.carmabs.ema.presentation

import com.carmabs.ema.android.base.EmaApplication
import com.carmabs.ema.presentation.injection.appInjection
import com.carmabs.injection.appDataInjection
import org.kodein.di.DI

/**
 *  *<p>
 * Copyright (c) 2020, Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 *
 * Created by: Carlos Mateo Benito on 21/1/19.
 */
class EmaSampleApplication : EmaApplication() {
    override fun injectAppModule(kodein: DI.MainBuilder): DI.Module? {
        kodein.import(appDataInjection())
        return appInjection(this)
    }
}