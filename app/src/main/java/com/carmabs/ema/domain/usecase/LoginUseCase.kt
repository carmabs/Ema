package com.carmabs.ema.domain.usecase


import com.carmabs.ema.domain.model.LoginRequest
import com.carmabs.ema.domain.model.User
import com.carmabs.ema.domain.repository.Repository


/**
 * Login to LDA
 *
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

class LoginUseCase(private val repository: Repository) {


    @Throws
    suspend fun doLogin(loginRequest: LoginRequest): User {
        return repository.login(loginRequest)
    }
}