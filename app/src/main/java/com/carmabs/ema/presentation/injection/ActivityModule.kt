package com.carmabs.ema.presentation.injection

import android.app.Activity
import com.carmabs.ema.android.ui.dialog.EmaBaseDialogProvider
import com.carmabs.ema.presentation.ui.error.EmaToolbarViewModel
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton

/**
 * Project: Ema
 * Created by: cmateob on 20/1/19.
 */

fun activityInjection(activity: Activity) = Kodein.Module(name = "ActivityModule") {

    bind<Activity>() with singleton { activity }

    bind<EmaToolbarViewModel>() with singleton { EmaToolbarViewModel() }


}