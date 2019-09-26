package com.carmabs.ema.presentation.injection

import android.app.Application
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton

/**
 * Project: Ema
 * Created by: cmateob on 20/1/19.
 */

fun appInjection(application: Application) = Kodein.Module(name = "AppModule") {

    bind<Application>() with singleton { application }

}