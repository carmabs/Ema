package com.carmabs.ema.presentation.ui.normalway.user

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.carmabs.ema.domain.model.User

class UserViewModel : ViewModel() {

    val user: MutableLiveData<User> = MutableLiveData()

    fun onStart(user:User?=null){
        user?.let { this.user.value = it }
    }


}
