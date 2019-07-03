package com.carmabs.ema.domain.repository

import com.carmabs.ema.domain.model.LoginRequest
import com.carmabs.ema.domain.model.User

/**
 * TODO: Add a class header comment.
 *

 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo</a>
 */

interface Repository {

    suspend fun login(loginRequest: LoginRequest): User
}