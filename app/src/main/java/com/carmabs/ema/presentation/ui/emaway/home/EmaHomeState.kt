package com.carmabs.ema.presentation.ui.emaway.home

import com.carmabs.ema.core.state.EmaBaseState

/**
 * Project: Ema
 * Created by: cmateob on 20/1/19.
 */
data class EmaHomeState(
        val userName:String="",
        val userPassword:String="",
        val showPassword:Boolean=false,
        val rememberuser:Boolean=false
        ) : EmaBaseState