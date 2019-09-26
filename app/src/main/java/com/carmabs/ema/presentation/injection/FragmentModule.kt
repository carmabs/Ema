package com.carmabs.ema.presentation.injection

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import com.carmabs.ema.android.ui.dialog.EmaBaseDialogProvider
import com.carmabs.ema.presentation.DIALOG_TAG_LOADING
import com.carmabs.ema.presentation.DIALOG_TAG_SIMPLE
import com.carmabs.ema.presentation.dialog.loading.LoadingDialogProvider
import com.carmabs.ema.presentation.dialog.simple.SimpleDialogProvider
import com.carmabs.ema.presentation.ui.error.EmaErrorViewModel
import com.carmabs.ema.presentation.ui.home.EmaHomeActivity
import com.carmabs.ema.presentation.ui.home.EmaHomeNavigator
import com.carmabs.ema.presentation.ui.home.EmaHomeViewModel
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

/**
 * Project: Ema
 * Created by: cmateob on 20/1/19.
 */

fun fragmentInjection(fragment: Fragment) = Kodein.Module(name = "FragmentModule") {

    bind<Fragment>() with singleton { fragment }

    bind<NavController>() with singleton { (fragment.requireActivity() as EmaHomeActivity).let { it.navController } }

    bind<FragmentManager>() with singleton { fragment.requireActivity().supportFragmentManager }

    bind<EmaBaseDialogProvider>(tag = DIALOG_TAG_SIMPLE) with provider { SimpleDialogProvider(instance()) }

    bind<EmaBaseDialogProvider>(tag = DIALOG_TAG_LOADING) with provider { LoadingDialogProvider(instance()) }

    bind<EmaHomeNavigator>() with singleton { EmaHomeNavigator(instance()) }

    bind<EmaHomeViewModel>() with singleton { EmaHomeViewModel(instance()) }

    bind<EmaErrorViewModel>() with singleton { EmaErrorViewModel() }


}