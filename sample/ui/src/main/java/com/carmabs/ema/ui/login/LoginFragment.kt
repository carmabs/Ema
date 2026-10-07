package com.carmabs.ema.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.carmabs.ema.android.extension.bindForUpdate
import com.carmabs.ema.android.extension.setTextWithCursorAtEnd
import com.carmabs.ema.core.constants.STRING_EMPTY
import com.carmabs.ema.core.model.EmaText
import com.carmabs.ema.core.navigator.EmaNavigator
import com.carmabs.ema.presentation.extension.fullName
import com.carmabs.ema.presentation.login.LoginAction
import com.carmabs.ema.presentation.login.LoginEvent
import com.carmabs.ema.presentation.login.LoginOverlap
import com.carmabs.ema.presentation.login.LoginState
import com.carmabs.ema.presentation.login.LoginViewModel
import com.carmabs.ema.sample.ema.R
import com.carmabs.ema.sample.ema.databinding.LoginFragmentBinding
import com.carmabs.ema.ui.base.BaseFragment
import com.carmabs.ema.ui.dialog.error.ErrorDialogData
import com.carmabs.ema.ui.dialog.error.ErrorDialogListener
import org.koin.android.ext.android.get


class LoginFragment :
    BaseFragment<LoginFragmentBinding, LoginState, LoginViewModel, LoginEvent>() {

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): LoginFragmentBinding {
        return LoginFragmentBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupListeners()
    }

    private fun LoginFragmentBinding.setupListeners() {
        etUser.addTextChangedListener {
            viewModel.dispatch(LoginAction.UserNameWritten(it?.toString() ?: STRING_EMPTY))
        }
        etPassword.addTextChangedListener {
            viewModel.dispatch(LoginAction.PasswordWritten(it?.toString() ?: STRING_EMPTY))
        }
        etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.dispatch(LoginAction.Login)
                true
            } else
                false
        }
        bLoginSign.setOnClickListener {
            viewModel.dispatch(LoginAction.Login)
        }
        tilLoginUser.setEndIconOnClickListener {
            viewModel.dispatch(LoginAction.DeleteUser)
        }
    }

    override fun LoginFragmentBinding.onState(state: LoginState) {
        //The text is written only when it differs, otherwise the cursor would jump to the end while typing
        bindForUpdate(state::userName) {
            if (etUser.text?.toString() != it)
                etUser.setTextWithCursorAtEnd(it)
        }
        bindForUpdate(state::userPassword) {
            if (etPassword.text?.toString() != it)
                etPassword.setTextWithCursorAtEnd(it)
        }
        bindForUpdate(state::userNameError) {
            tilLoginUser.error = if (it) getString(R.string.login_error_user_empty) else null
        }
        bindForUpdate(state::passwordError) {
            tilLoginPassword.error = if (it) getString(R.string.login_error_password_empty) else null
        }
        bindForUpdate(state::isLoading) {
            onLoading(it)
        }
        bindForUpdate(state::overlap) {
            onOverlap(it)
        }
    }

    private fun LoginFragmentBinding.onLoading(isLoading: Boolean) {
        //The button is not disabled to keep its color behind the progress indicator
        bLoginSign.isClickable = !isLoading
        bLoginSign.text = if (isLoading) STRING_EMPTY else getString(R.string.login_access)
        pbLoginSign.isVisible = isLoading
        tilLoginUser.isEnabled = !isLoading
        tilLoginPassword.isEnabled = !isLoading
    }

    private fun onOverlap(overlap: LoginOverlap?) {
        when (overlap) {
            null -> hideDialog()

            LoginOverlap.ErrorBadCredentials -> {
                showError(ErrorDialogData(
                    EmaText.id(R.string.login_error_fail_title),
                    EmaText.id(R.string.login_error_fail),
                ), object : ErrorDialogListener {
                    override fun onConfirmClicked() {
                        viewModel.dispatch(LoginAction.Error.BadCredentialsAccepted)
                    }

                    override fun onBackPressed() {
                        viewModel.dispatch(LoginAction.Error.BackPressed)
                    }

                })
            }
        }
    }

    override fun provideViewModel(): LoginViewModel {
        return get()
    }

    override suspend fun LoginFragmentBinding.onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.LoginSuccess -> navigate(event)

            is LoginEvent.LastUserAdded -> {
                showMessage(getString(R.string.login_last_user_added, event.user.fullName))
            }

            is LoginEvent.Message -> {
                showMessage(getString(R.string.login_welcome, event.userName))
            }
        }
    }

    override val navigator: EmaNavigator<LoginEvent> = LoginNavigator(this)
}
