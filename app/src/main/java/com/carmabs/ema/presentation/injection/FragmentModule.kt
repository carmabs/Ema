package com.carmabs.ema.presentation.injection

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.NavHost
import com.carmabs.ema.MockRepository
import com.carmabs.ema.android.ui.EmaFragmentActivity
import com.carmabs.ema.domain.repository.Repository
import com.carmabs.ema.domain.usecase.LoginUseCase
import com.carmabs.ema.presentation.dialog.DialogProvider
import com.carmabs.ema.presentation.dialog.loading.LoadingDialogProvider
import com.carmabs.ema.presentation.dialog.simple.SimpleDialogProvider
import com.carmabs.ema.presentation.ui.emaway.home.EmaHomeActivity
import com.carmabs.ema.presentation.ui.emaway.home.EmaHomeNavigator
import com.carmabs.ema.presentation.ui.emaway.home.EmaHomeViewModel
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

    bind<NavController>() with singleton { (fragment.activity as EmaHomeActivity).let { it.navController } }

    bind<FragmentManager>() with singleton { fragment.activity!!.supportFragmentManager }

    bind<Repository>() with provider { MockRepository() }

    bind<DialogProvider>(tag = "SIMPLE") with provider { SimpleDialogProvider(instance()) }

    bind<DialogProvider>(tag = "LOADING") with provider { LoadingDialogProvider(instance()) }

    bind<EmaHomeNavigator>() with singleton { EmaHomeNavigator(instance()) }

    bind<LoginUseCase>() with singleton { LoginUseCase(instance()) }

    bind<EmaHomeViewModel>() with singleton { EmaHomeViewModel(instance()) }


}