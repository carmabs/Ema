package com.carmabs.domain.usecase

import com.carmabs.domain.model.LoginRequest
import com.carmabs.domain.model.User
import com.carmabs.domain.repository.Repository
import com.carmabs.ema.core.usecase.EmaUseCase


/**
 * Login
 *
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

class LoginUseCase(private val repository: Repository) : EmaUseCase<LoginRequest, User>() {

    override suspend fun useCaseFunction(input: LoginRequest): User {
        return repository.login(input)
    }
}