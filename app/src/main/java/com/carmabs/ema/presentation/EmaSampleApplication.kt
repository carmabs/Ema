package com.carmabs.ema.presentation

import com.carmabs.ema.android.base.EmaApplication
import com.carmabs.ema.presentation.injection.appInjection
import com.carmabs.injection.appDataInjection
import org.kodein.di.Kodein

/**
 * Project: Ema
 * Created by: cmateob on 21/1/19.
 */
class EmaSampleApplication : EmaApplication() {
    override fun injectAppModule(kodein: Kodein.MainBuilder): Kodein.Module? {
        kodein.import(appDataInjection())
        return appInjection(this)
    }
}