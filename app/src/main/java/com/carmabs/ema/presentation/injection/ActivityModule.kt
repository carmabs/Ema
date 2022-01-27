package com.carmabs.ema.presentation.injection

import android.app.Activity
import androidx.navigation.NavController
import com.carmabs.ema.android.ui.EmaFragmentActivity
import com.carmabs.ema.presentation.ui.backdata.EmaAndroidBackToolbarViewModel
import com.carmabs.ema.presentation.ui.backdata.EmaBackNavigator
import com.carmabs.ema.presentation.ui.backdata.EmaBackToolbarViewModel
import com.carmabs.ema.presentation.ui.unlogged.*
import com.carmabs.ema.presentation.ui.home.EmaAndroidHomeToolbarViewModel
import com.carmabs.ema.presentation.ui.home.EmaHomeNavigator
import com.carmabs.ema.presentation.ui.home.EmaHomeToolbarViewModel
import com.carmabs.ema.presentation.ui.unlogged.EmaUnloggedToolbarViewModel
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

/**
 *  *<p>
 * Copyright (c) 2020, Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 *
 * Created by: Carlos Mateo Benito on 20/1/19.
 */

fun activityInjection(activity: Activity) = DI.Module(name = "ActivityModule") {

    bind<Activity>() with singleton { activity }

    bind<NavController>() with singleton { (activity as EmaFragmentActivity<*>).let { it.navController } }

    bind<EmaUnloggedNavigator>() with singleton { EmaUnloggedNavigator(instance(),instance()) }

    bind<EmaHomeNavigator>() with singleton { EmaHomeNavigator(instance(),instance()) }

    bind<EmaBackNavigator>() with singleton { EmaBackNavigator(instance(),instance()) }

    bind<EmaBackToolbarViewModel>() with singleton { EmaBackToolbarViewModel() }

    bind<EmaHomeToolbarViewModel>() with singleton { EmaHomeToolbarViewModel() }

    bind<EmaUnloggedToolbarViewModel>() with singleton { EmaUnloggedToolbarViewModel() }

    bind<EmaAndroidBackToolbarViewModel>() with singleton { EmaAndroidBackToolbarViewModel(instance()) }

    bind<EmaAndroidHomeToolbarViewModel>() with singleton { EmaAndroidHomeToolbarViewModel(instance()) }
}