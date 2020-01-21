package com.carmabs.ema.presentation.injection

import android.app.Activity
import androidx.navigation.NavController
import com.carmabs.ema.android.ui.EmaFragmentActivity
import com.carmabs.ema.presentation.ui.backdata.EmaBackNavigator
import com.carmabs.ema.presentation.ui.backdata.EmaBackToolbarViewModel
import com.carmabs.ema.presentation.ui.error.EmaErrorNavigator
import com.carmabs.ema.presentation.ui.error.EmaErrorToolbarViewModel
import com.carmabs.ema.presentation.ui.home.EmaHomeNavigator
import com.carmabs.ema.presentation.ui.home.EmaHomeToolbarViewModel
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

/**
 *  *<p>
 * Copyright (c) 2020, Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 *
 * Created by: Carlos Mateo Benito on 20/1/19.
 */

fun activityInjection(activity: Activity) = Kodein.Module(name = "ActivityModule") {

    bind<Activity>() with singleton { activity }

    bind<EmaErrorToolbarViewModel>() with singleton { EmaErrorToolbarViewModel() }

    bind<NavController>() with singleton { (activity as EmaFragmentActivity).let { it.navController } }

    bind<EmaErrorNavigator>() with singleton { EmaErrorNavigator(instance(),instance()) }

    bind<EmaHomeNavigator>() with singleton { EmaHomeNavigator(instance(),instance()) }

    bind<EmaBackNavigator>() with singleton { EmaBackNavigator(instance()) }

    bind<EmaBackToolbarViewModel>() with singleton { EmaBackToolbarViewModel() }

    bind<EmaHomeToolbarViewModel>() with singleton { EmaHomeToolbarViewModel() }

}