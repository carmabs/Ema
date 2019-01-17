package com.carmabs.ema.domain.usecase


import com.carmabs.ema.domain.LoginRequest
import com.carmabs.ema.domain.Repository
import com.carmabs.ema.domain.User
import kotlinx.coroutines.*
import java.lang.Exception


/**
 * Login to LDA
 *
 * <p>
 * Copyright (c) 2018, Babel Sistemas de Información. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:carlos.mateo@babel.es”>Carlos Mateo</a>
 */

class LoginUseCase(private val repository: Repository) {


    @Throws
    suspend fun doLogin(loginRequest:LoginRequest): User {

        val job = Job()
        val scope = CoroutineScope(Dispatchers.Main + job)
        var user : User? = null

        val workerJob = scope.launch {
            user = repository.login(loginRequest)
        }

        workerJob.join()

        return user?:throw Exception("Login failed")
    }
}