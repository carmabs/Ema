package com.carmabs.ema.presentation.ui.normalway.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.carmabs.ema.domain.usecase.LoginUseCase

/**
 * Project: Ema
 * Created by: cmateob on 19/1/19.
 */
data class HomeFactory(val loginUseCase: LoginUseCase) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return HomeViewModel(loginUseCase) as T
    }
}