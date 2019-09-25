package com.carmabs.injection

import com.carmabs.domain.repository.Repository
import com.carmabs.domain.usecase.LoginUseCase
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

/**
 * Project: Ema
 * Created by: cmateob on 20/1/19.
 */

fun appDataInjection() = Kodein.Module(name = "AppDataModule") {

   bind<Repository>() with singleton { com.carmabs.data.repository.MockRepository() }

   bind<LoginUseCase>() with provider { LoginUseCase(instance()) }
}