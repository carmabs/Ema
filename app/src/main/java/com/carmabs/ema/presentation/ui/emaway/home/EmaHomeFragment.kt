package com.carmabs.ema.presentation.ui.emaway.home

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import com.carmabs.ema.R
import com.carmabs.ema.android.ui.EmaFragment
import com.carmabs.ema.core.navigator.EmaBaseNavigator
import com.carmabs.ema.core.state.EmaExtraData
import com.carmabs.ema.domain.exception.LoginException
import com.carmabs.ema.domain.exception.PasswordEmptyException
import com.carmabs.ema.domain.exception.UserEmptyException
import com.carmabs.ema.presentation.dialog.DialogProvider
import com.carmabs.ema.presentation.dialog.loading.LoadingDialogData
import com.carmabs.ema.presentation.dialog.simple.SimpleDialogData
import com.carmabs.ema.presentation.dialog.simple.SimpleDialogListener
import com.carmabs.ema.presentation.injection.fragmentInjection
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.layout_password.*
import kotlinx.android.synthetic.main.layout_user.*
import org.kodein.di.Kodein
import org.kodein.di.generic.instance

/**
 * Project: Ema
 * Created by: cmateob on 20/1/19.
 */
class EmaHomeFragment : EmaFragment<EmaHomeState, EmaHomeViewModel, EmaHomeNavigator.Navigation>() {

    override val inputStateKey: String? = null

    override val fragmentViewModelScope: Boolean = true

    override fun getFragmentLayout(): Int = R.layout.fragment_home

    override fun injectFragmentModule(kodein: Kodein.MainBuilder): Kodein.Module = fragmentInjection(this)

    override val viewModelSeed: EmaHomeViewModel by instance()

    override val navigator: EmaHomeNavigator  by instance()

    private val errorDialog: DialogProvider by instance(tag = "SIMPLE")

    private val loadingDialog: DialogProvider by instance(tag = "LOADING")

    override fun onInitialized(viewModel: EmaHomeViewModel) {
        setupButtons(viewModel)
        setupDialog(viewModel)
    }

    private fun setupDialog(viewModel: EmaHomeViewModel) {
        errorDialog.setDialogListener(object : SimpleDialogListener {
            override fun onCancelClicked() {
                viewModel.onActionDialogErrorCancel()
            }

            override fun onBackPressed() {
                viewModel.onActionDialogErrorCancel()
            }

            override fun onConfirmClicked() {
                viewModel.onActionDialogErrorAccept()
            }

        })
    }

    private fun setupButtons(viewModel: EmaHomeViewModel) {
        //swLightLoginRememberPassword.setOnCheckedChangeListener { _, _ -> viewModel.onActionRemember() }
        ivHomeTouchEmptyUser.setOnClickListener { viewModel.onActionDeleteUser() }
        ivHomePassEmptyPassword.setOnClickListener { viewModel.onActionDeletePassword() }
        ivHomeTouchEmptyUser.setOnClickListener { viewModel.onActionShowPassword() }
        etUser.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onActionUserWrite(s?.toString() ?: "")
            }
        })
        etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onActionPasswordWrite(s?.toString() ?: "")
            }

        })
        bLightLoginSign.setOnClickListener {
            viewModel.onActionLogin()
        }
    }

    private fun checkError(textView: TextView) {
        textView.visibility = View.VISIBLE
    }

    private fun hideErrors() {
        tvLightLoginErrorUser.visibility = View.GONE
        tvLightLoginErrorPassword.visibility = View.GONE
        errorDialog.hide()
    }

    private fun hideLoading(){
        loadingDialog.hide()
    }

    private fun showErrorDialog() {
        errorDialog.show(SimpleDialogData(
                "INVALID CREDENTIALS",
                "Your credentials are invalid, please retry it",
                accept = "Accept"
        ))
    }

    private fun showLoadingDialog(){
            loadingDialog.show(
                    LoadingDialogData(
                            "Loading",
                            "Getting user data..."
                    ))
    }


    override fun onStateNormal(data: EmaHomeState) {
        hideErrors()
        hideLoading()
        if (etUser.text.toString() != data.userName)
            etUser.setText(data.userName)
        if (etPassword.text.toString() != data.userPassword)
            etPassword.setText(data.userPassword)
        if(swLightLoginRememberPassword.isChecked !=  data.rememberuser)
            swLightLoginRememberPassword.isChecked = data.rememberuser
    }

    override fun onStateLoading(data: EmaExtraData) {
        showLoadingDialog()
    }

    override fun onSingleEvent(data: EmaExtraData) {

    }

    override fun onStateError(error: Throwable) {
        when (error) {
            is UserEmptyException -> checkError(tvLightLoginErrorUser)
            is PasswordEmptyException -> checkError(tvLightLoginErrorPassword)
            is LoginException -> showErrorDialog()
        }
    }
}