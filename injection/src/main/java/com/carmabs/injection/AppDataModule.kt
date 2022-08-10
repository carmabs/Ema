package com.carmabs.injection

import com.carmabs.data.manager.AndroidResourceManager
import com.carmabs.data.repository.MockRepository
import com.carmabs.domain.manager.ResourceManager
import com.carmabs.domain.repository.Repository
import com.carmabs.domain.usecase.LoginUseCase


import org.koin.core.component.inject
import org.kodein.di.provider
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

fun appDataInjection() = DI.Module(name = "AppDataModule") {

   bind<Repository>() with singleton { MockRepository() }

   bind<LoginUseCase> { LoginUseCase(instance()) }

   bind<ResourceManager>() with singleton { AndroidResourceManager(instance()) }
}