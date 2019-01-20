package com.carmabs.ema

import com.carmabs.ema.domain.exception.LoginException
import com.carmabs.ema.domain.model.LoginRequest
import com.carmabs.ema.domain.model.User
import com.carmabs.ema.domain.repository.Repository
import kotlinx.coroutines.delay


/**
 * Project: Ema
 * Created by: cmateob on 19/1/19.
 */
class MockRepository : Repository {

    override suspend fun login(loginRequest: LoginRequest): User {
        delay(2000)
       if(loginRequest.name.equals("1234") && loginRequest.password=="1234")
           return User("Babel","Sistemas de informacion")
        else throw LoginException()
    }
}