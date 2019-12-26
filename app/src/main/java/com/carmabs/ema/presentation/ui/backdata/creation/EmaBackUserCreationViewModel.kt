package com.carmabs.ema.presentation.ui.backdata.creation

import com.carmabs.domain.manager.ResourceManager
import com.carmabs.ema.android.viewmodel.EmaViewModel
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.presentation.ui.backdata.EmaBackNavigator
import com.carmabs.ema.presentation.ui.backdata.userlist.EmaBackUserModel

/**
 * <p>
 * Copyright (c) 2019, Babel Sistemas de Información. All rights reserved.
 * </p>
 *
 * @author <a href=“mailto:carlos.mateo@babel.es”>Carlos Mateo</a>
 *
 * Date: 2019-11-07
 */

class EmaBackUserCreationViewModel(private val resourceManager: ResourceManager) : EmaViewModel<EmaBackUserCreationState, EmaBackNavigator.Navigation>() {

    companion object {
        const val RESULT_USER = 0
    }

    override fun createInitialViewState(): EmaBackUserCreationState {
        return EmaBackUserCreationState()
    }

    override fun onStartFirstTime(statePreloaded: Boolean) {
        loading()
    }

    fun onActionAddUser(name: String, surname: String) {
        when {
            name.isEmpty() -> sendSingleEvent(EmaExtraData(extraData = resourceManager.getResultErrorFillName()))
            surname.isEmpty() -> sendSingleEvent(EmaExtraData(extraData = resourceManager.getResultErrorFillSurname()))
            else -> {
                setResult(RESULT_USER, EmaBackUserModel(
                        name = name,
                        surname = surname
                ))
                navigateBack()
            }
        }
    }

    fun onActionNameWrite(name: String) {
        updateViewState {
            copy(name = name)
        }
    }

    fun onActionSurnameWrite(surname: String) {
        updateViewState {
            copy(surname = surname)
        }
    }
}