package com.carmabs.data.repository

import com.carmabs.domain.exception.LoginException
import com.carmabs.domain.model.LoginRequest
import com.carmabs.domain.model.User
import com.carmabs.domain.repository.Repository
import kotlinx.coroutines.delay


/**
 * Project: Ema
 * Created by: cmateob on 19/1/19.
 */
class MockRepository : Repository {

    override suspend fun login(loginRequest: LoginRequest): User {
        delay(2000)
       if(loginRequest.name.equals("Admin",true) && loginRequest.password=="1234")
           return User("EMA", "MVVM Architecture, powered by Carmabs")
        else throw LoginException()
    }
}