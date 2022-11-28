package com.carmabs.data.repository

import com.carmabs.domain.exception.LoginException
import com.carmabs.domain.model.LoginRequest
import com.carmabs.domain.model.User
import com.carmabs.domain.repository.Repository
import kotlinx.coroutines.delay


/**
 *  *<p>
 * Copyright (c) 2020, Carmabs. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:apps.carmabs@gmail.com”>Carlos Mateo Benito</a>
 *
 * Created by: Carlos Mateo Benito on 19/1/19.
 */
class MockRepository : Repository {

    override suspend fun login(loginRequest: LoginRequest): User {
        delay(2000)
       if(loginRequest.name.equals("Admin",true) && loginRequest.password=="1234")
           return User("Admin", "EMA based on MVVM Architecture, powered by Carmabs")
        else throw LoginException()
    }
}