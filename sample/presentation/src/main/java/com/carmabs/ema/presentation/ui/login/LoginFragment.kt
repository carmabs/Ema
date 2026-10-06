package com.carmabs.ema.presentation.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.carmabs.ema.android.base.EmaSingleToast
import com.carmabs.ema.android.di.injectDirect
import com.carmabs.ema.android.extension.getFormattedString
import com.carmabs.ema.android.extension.setTextWithCursorAtEnd
import com.carmabs.ema.core.constants.STRING_EMPTY
import com.carmabs.ema.core.model.EmaText
import com.carmabs.ema.core.navigator.EmaNavigator
import com.carmabs.ema.presentation.base.BaseFragment
import com.carmabs.ema.presentation.dialog.error.ErrorDialogData
import com.carmabs.ema.presentation.dialog.error.ErrorDialogListener
import com.carmabs.ema.sample.ema.R
import com.carmabs.ema.sample.ema.databinding.LoginFragmentBinding


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
        layoutLoginUser.etUser.addTextChangedListener {
            viewModel.dispatch(LoginAction.UserNameWritten(it?.toString() ?: STRING_EMPTY))
        }
        layoutLoginPassword.etPassword.addTextChangedListener {
            viewModel.dispatch(LoginAction.PasswordWritten(it?.toString() ?: STRING_EMPTY))
        }
        bLoginSign.setOnClickListener {
            viewModel.dispatch(LoginAction.Login)
        }
        layoutLoginUser.ivHomeTouchEmptyUser.setOnClickListener {
            viewModel.dispatch(LoginAction.DeleteUser)
        }
    }

    private fun onOverlap(overlap: LoginOverlap?) {
        when (overlap) {
            null -> hideDialog()

            LoginOverlap.Loading -> showLoading()

            LoginOverlap.ErrorBadCredentials -> {
                showError(ErrorDialogData(
                    EmaText.id(R.string.general_error_title),
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

            LoginOverlap.ErrorUserEmpty -> {
                showError(ErrorDialogData(
                    EmaText.id(R.string.general_error_title),
                    EmaText.id(R.string.login_error_user_empty),
                ), object : ErrorDialogListener {
                    override fun onConfirmClicked() {
                        viewModel.dispatch(LoginAction.Error.UserEmptyAccepted)
                    }

                    override fun onBackPressed() {
                        viewModel.dispatch(LoginAction.Error.BackPressed)
                    }

                })
            }

            LoginOverlap.ErrorPasswordEmpty -> {
                showError(ErrorDialogData(
                    EmaText.id(R.string.general_error_title),
                    EmaText.id(R.string.login_error_password_empty),
                ), object : ErrorDialogListener {
                    override fun onConfirmClicked() {
                        viewModel.dispatch(LoginAction.Error.PasswordEmptyAccepted)
                    }

                    override fun onBackPressed() {
                        viewModel.dispatch(LoginAction.Error.BackPressed)
                    }

                })
            }

        }

    }

    override fun provideViewModel(): LoginViewModel {
        return injectDirect()
    }

    override fun LoginFragmentBinding.onState(state: LoginState) {
        bindForUpdate(state::userName) {
            layoutLoginUser.etUser.setTextWithCursorAtEnd(it)
        }
        bindForUpdate(state::userPassword) {
            layoutLoginPassword.etPassword.setTextWithCursorAtEnd(it)
        }
        bindForUpdate(state::overlap) {
            onOverlap(it)
        }
    }


    override suspend fun LoginFragmentBinding.onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.LoginSuccess -> navigate(event)

            is LoginEvent.LastUserAdded -> {
                val user = event.user
                EmaSingleToast.show(
                    requireContext(),
                    R.string.login_last_user_added.getFormattedString(
                        requireContext(),
                        "${user.name} ${user.surname})"
                    ),
                    Toast.LENGTH_SHORT
                )
            }

            is LoginEvent.Message -> {
                EmaSingleToast.show(
                    requireContext(),
                    R.string.home_welcome.getFormattedString(requireContext(),event.userName),
                    Toast.LENGTH_SHORT
                )

            }
        }
    }

    override val navigator: EmaNavigator<LoginEvent> = LoginNavigator(this)
}
