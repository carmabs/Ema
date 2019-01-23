package com.carmabs.ema.presentation.ui.normalway.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.carmabs.ema.domain.exception.UserEmptyException
import com.carmabs.ema.domain.model.LoginRequest
import com.carmabs.ema.domain.model.User
import com.carmabs.ema.domain.usecase.LoginUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HomeViewModel(private val loginUseCase: LoginUseCase) : ViewModel() {

    val login: MutableLiveData<LoginRequest> = MutableLiveData()
    val error: MutableLiveData<Exception> = MutableLiveData()
    val user: MutableLiveData<User?> = MutableLiveData()
    val loading:MutableLiveData<Boolean> = MutableLiveData()
    val showPassword:MutableLiveData<Boolean> = MutableLiveData()
    val rememberUser:MutableLiveData<Boolean> = MutableLiveData()

    private var loginRequest:LoginRequest? = null

    fun onActionLogin(loginRequest: LoginRequest) {

        when {
            loginRequest.name.isEmpty() -> error.value = UserEmptyException()
            loginRequest.password.isEmpty() -> error.value = UserEmptyException()
            else -> doLogin(loginRequest)
        }
    }

    fun onStart(){
        if(loginRequest!=null)
            login.value = loginRequest
    }

    private fun doLogin(loginRequest: LoginRequest) {

        val job = Job()
        val coroutineScope = CoroutineScope(Dispatchers.Main + job)
        coroutineScope.launch {
            loading.value = true
            try {
                val user = loginUseCase.doLogin(loginRequest)
                this@HomeViewModel.user.value = user
            } catch (e: Exception) {
                error.value = e
            } finally {
                this@HomeViewModel.loading.value = false
            }
        }


    }

    fun onActionShowPassword() {
        val show = showPassword.value?:true
        showPassword.value = (!show)
    }

    fun onActionRemember(checked: Boolean) {
        rememberUser.value = checked
    }

    fun onActionDeletePassword() {
        login.value = login.value?.copy(password = "")?: LoginRequest()
    }

    fun onActionDeleteUser() {
        login.value = login.value?.copy(name = "")?: LoginRequest()
    }

    fun onActionPasswordWrite(password: String) {
        loginRequest = loginRequest?.copy(password = password)?: LoginRequest(password = password)
    }

    fun onActionUserWrite(user: String) {
        loginRequest = loginRequest?.copy(name = user)?: LoginRequest(name = user)
    }

    fun onActionDialogErrorCancel() {
        error.value = null
    }

    fun onActionDialogErrorAccept() {
        error.value = null
    }
}
