package com.carmabs.ema.presentation.ui.defaultvm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.carmabs.ema.domain.LoginRequest
import com.carmabs.ema.domain.User
import com.carmabs.ema.domain.exception.UserEmptyException
import com.carmabs.ema.domain.usecase.LoginUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception

class DefaultVmViewModel(private val loginUseCase: LoginUseCase) : ViewModel() {

    val user: MutableLiveData<User> = MutableLiveData()
    val error: MutableLiveData<Exception> = MutableLiveData()

    fun onActionLogin(loginRequest: LoginRequest) {

        when{
            loginRequest.name.isEmpty() -> error.value = UserEmptyException()
            loginRequest.password.isEmpty() -> error.value = UserEmptyException()
            else -> doLogin(loginRequest)

        }


    }

    private fun doLogin(loginRequest: LoginRequest){
        try {
            val job = Job()
            val coroutineScope = CoroutineScope(Dispatchers.Main + job)
            coroutineScope.launch {
                user.value = loginUseCase.doLogin(loginRequest)
            }

        }catch (e:Exception){
            error.value = e
        }
    }
}
